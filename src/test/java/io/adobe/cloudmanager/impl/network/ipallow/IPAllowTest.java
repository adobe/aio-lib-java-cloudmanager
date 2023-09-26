package io.adobe.cloudmanager.impl.network.ipallow;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.adobe.aio.ims.feign.AuthInterceptor;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.IPAllowApi;
import io.adobe.cloudmanager.IPAllowList;
import io.adobe.cloudmanager.impl.AbstractApiTest;
import io.adobe.cloudmanager.impl.generated.EmbeddedProgram;
import io.adobe.cloudmanager.impl.generated.IPAllowedList;
import io.adobe.cloudmanager.impl.program.ProgramImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class IPAllowTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("network/ipallow/get.json");
  private static final JsonBody LIST_BODY = loadBodyJson("network/ipallow/list.json");
  private static final JsonBody GET_BINDING_BODY = loadBodyJson("network/ipallow/get-binding.json");
  private static final JsonBody LIST_BINDING_BODY = loadBodyJson("network/ipallow/list-binding.json");

  private IPAllowApi underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = IPAllowApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get IP Allow List: %s/api/program/1/ipAllowlist/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(get).respond(response().withBody(GET_BODY));
    IPAllowList ipl = underTest.get("1", "1");
    assertNotNull(ipl);
    assertEquals(2, ipl.listCidr().size(), "CIDR list correct.");
    assertEquals(1, ipl.listBindings().size(), "Binding list correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void update_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(put).respond(response().withStatusCode(BAD_REQUEST_400.code()));

    String name = "newname";
    List<IPAllowList.Cidr> cidrs = new ArrayList<>();
    cidrs.add(new IPAllowList.Cidr("33.44.55.66", "16"));
    cidrs.add(new IPAllowList.Cidr("99.88.77.11", "8"));

    List<IPAllowList.Binding> bindings = new ArrayList<>();
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PUBLISH));
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PREVIEW));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.update("1", "1", name, cidrs, bindings), "Exception thrown.");
    assertEquals(String.format("Cannot update IP Allow List: %s/api/program/1/ipAllowlist/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(put);
    client.clear(put);
  }

  @Test
  void update_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/ipAllowlist/1")
        .withBody(
            json("{ " +
                "\"id\": \"1\", " +
                "\"programId\": \"1\", " +
                "\"name\": \"newname\", " +
                "\"ipCidrSet\": [\"33.44.55.66/16\", \"99.88.77.11/8\"], " +
                "\"bindings\": [ " +
                "{ \"programId\": \"1\", \"ipAllowListId\": \"1\", \"environmentId\": \"1\", \"tier\": \"preview\" }, " +
                "{ \"programId\": \"1\", \"ipAllowListId\": \"1\", \"environmentId\": \"1\", \"tier\": \"publish\" } " +
                "] }")
        );
    client.when(put).respond(response().withBody(GET_BODY));

    String name = "newname";
    List<IPAllowList.Cidr> cidrs = new ArrayList<>();
    cidrs.add(new IPAllowList.Cidr("33.44.55.66", "16"));
    cidrs.add(new IPAllowList.Cidr("99.88.77.11", "8"));

    List<IPAllowList.Binding> bindings = new ArrayList<>();
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PUBLISH));
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PREVIEW));
    underTest.update("1", "1", name, cidrs, bindings);
    client.verify(put);
    client.clear(put);
  }

  @Test
  void delete_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(del).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.delete("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot delete IP Allow List: %s/api/program/1/ipAllowlist/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(del).respond(response().withStatusCode(ACCEPTED_202.code()).withBody(GET_BODY));
    underTest.delete("1", "1");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlists");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown.");
    assertEquals(String.format("Cannot list IP Allow Lists: %s/api/program/1/ipAllowlists (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlists");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<IPAllowList> ipls =  underTest.list(new ProgramImpl(mock, null));
    assertNotNull(ipls);
    assertEquals(2, ipls.size(), "Size correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void create_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlists");
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));

    String name = "newname";
    List<IPAllowList.Cidr> cidrs = new ArrayList<>();
    cidrs.add(new IPAllowList.Cidr("33.44.55.66", "16"));
    cidrs.add(new IPAllowList.Cidr("99.88.77.11", "8"));

    List<IPAllowList.Binding> bindings = new ArrayList<>();
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PUBLISH));
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PREVIEW));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.create("1", name, cidrs, bindings), "Exception thrown.");
    assertEquals(String.format("Cannot create IP Allow List: %s/api/program/1/ipAllowlists (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void create_failure_412() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlists");
    client.when(post).respond(response().withStatusCode(PRECONDITION_FAILED_412.code()));

    String name = "newname";
    List<IPAllowList.Cidr> cidrs = new ArrayList<>();
    cidrs.add(new IPAllowList.Cidr("33.44.55.66", "16"));
    cidrs.add(new IPAllowList.Cidr("99.88.77.11", "8"));

    List<IPAllowList.Binding> bindings = new ArrayList<>();
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PUBLISH));
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PREVIEW));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.create("1", name, cidrs, bindings), "Exception thrown.");
    assertEquals("IP Allow Lists are not supported on non-Cloud Service programs.", exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void create_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/ipAllowlists")
        .withBody(
            json("{ " +
                "\"programId\": \"1\", " +
                "\"name\": \"newname\", " +
                "\"ipCidrSet\": [\"33.44.55.66/16\", \"99.88.77.11/8\"], " +
                "\"bindings\": [ " +
                "{ \"programId\": \"1\", \"environmentId\": \"1\", \"tier\": \"preview\" }, " +
                "{ \"programId\": \"1\", \"environmentId\": \"1\", \"tier\": \"publish\" } " +
                "] }")
        );
    client.when(post).respond(response().withStatusCode(CREATED_201.code()).withBody(GET_BODY));

    String name = "newname";
    List<IPAllowList.Cidr> cidrs = new ArrayList<>();
    cidrs.add(new IPAllowList.Cidr("33.44.55.66", "16"));
    cidrs.add(new IPAllowList.Cidr("99.88.77.11", "8"));

    List<IPAllowList.Binding> bindings = new ArrayList<>();
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PUBLISH));
    bindings.add(new IPAllowList.Binding("1", Environment.Tier.PREVIEW));
    IPAllowList created = underTest.create("1", name, cidrs, bindings);
    assertNotNull(created);
    client.verify(post);
    client.clear(post);
  }

  @Test
  void get_binding_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/binding/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getBinding("1", "1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get IP Allow List binding: %s/api/program/1/ipAllowlist/1/binding/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_binding_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/binding/1");
    client.when(get).respond(response().withBody(GET_BINDING_BODY));
    IPAllowList.Binding binding = underTest.getBinding("1", "1", "1");
    assertNotNull(binding);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_binding_via_ipAllow() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(get).respond(response().withBody(GET_BODY));
    IPAllowList.Binding binding = underTest.get("1", "1").getBinding("1");
    assertNotNull(binding);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void delete_binding_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/binding/1");
    client.when(del).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class , () -> underTest.deleteBinding("1", "1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot delete IP Allow List binding: %s/api/program/1/ipAllowlist/1/binding/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_binding_success(@Mock IPAllowedList ipl, @Mock IPAllowList.Binding binding) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(ipl.getProgramId()).thenReturn("1");
    when(ipl.getId()).thenReturn("1");
    when(binding.getId()).thenReturn("1");
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/binding/1");
    client.when(del).respond(response().withBody(GET_BINDING_BODY));
    new IPAllowListImpl(ipl, underTest).delete(binding);
    client.verify(del);
    client.clear(del);
  }

  @Test
  void retry_binding_failure_404(@Mock IPAllowList.Binding mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/binding/1/retry");
    client.when(put).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class , () -> underTest.retryBinding("1", "1", mock), "Exception thrown.");
    assertEquals(String.format("Cannot rebind IP Allow List binding: %s/api/program/1/ipAllowlist/1/binding/1/retry (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(put);
    client.clear(put);
  }

  @Test
  void retry_binding_success(@Mock IPAllowedList ipl, @Mock IPAllowList.Binding binding) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(ipl.getProgramId()).thenReturn("1");
    when(ipl.getId()).thenReturn("1");
    when(binding.getId()).thenReturn("1");
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/binding/1/retry");
    client.when(put).respond(response().withBody(GET_BINDING_BODY));
    new IPAllowListImpl(ipl, underTest).retry(binding);
    client.verify(put);
    client.clear(put);
  }

  @Test
  void list_binding_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/bindings");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listBindings("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot list IP Allow List bindings: %s/api/program/1/ipAllowlist/1/bindings (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_binding_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/bindings");
    client.when(get).respond(response().withBody(LIST_BINDING_BODY));
    Collection<IPAllowList.Binding> bindings = underTest.listBindings("1", "1");
    assertEquals(2, bindings.size(), "Size correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void create_binding_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/bindings");
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.createBinding("1", "1", "1", Environment.Tier.PUBLISH), "Exception thrown.");
    assertEquals(String.format("Cannot create IP Allow List binding: %s/api/program/1/ipAllowlist/1/bindings (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void create_binding_failure_412() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/bindings");
    client.when(post).respond(response().withStatusCode(PRECONDITION_FAILED_412.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.createBinding("1", "1", "1", Environment.Tier.PUBLISH), "Exception thrown.");
    assertEquals("IP Allow Lists are not supported on non-Cloud Service programs.", exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }


  @Test
  void create_binding_success(@Mock IPAllowedList mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest post = request().withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/ipAllowlist/1/bindings")
        .withBody(json( "{ \"environmentId\": \"1\", \"tier\": \"publish\" }"));
    client.when(post).respond(response().withBody(GET_BINDING_BODY));
    IPAllowList.Binding binding = new IPAllowListImpl(mock, underTest).createBinding("1", Environment.Tier.PUBLISH);
    assertNotNull(binding);
    client.verify(post);
    client.clear(post);
  }

  @Test
  void get_binding_via_ipAllow_env_tier() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(get).respond(response().withBody(GET_BODY));
    IPAllowList ipl = underTest.get("1", "1");

    Optional<IPAllowList.Binding> binding = ipl.getBinding("1", Environment.Tier.PUBLISH);
    assertTrue(binding.isPresent());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void delete_binding_success_env_tier_not_found() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(get).respond(response().withBody(GET_BODY));
    IPAllowList ipl = underTest.get("1", "1");
    ipl.deleteBinding("2", Environment.Tier.PUBLISH);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void delete_binding_success_env_tier_via_ipallow() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/ipAllowlist/1/binding/1");
    client.when(del).respond(response().withBody(GET_BINDING_BODY));
    IPAllowList ipl = underTest.get("1", "1");
    ipl.deleteBinding("1", Environment.Tier.PUBLISH);
    client.verify(get, del);
    client.clear(get);
    client.clear(del);
  }
}

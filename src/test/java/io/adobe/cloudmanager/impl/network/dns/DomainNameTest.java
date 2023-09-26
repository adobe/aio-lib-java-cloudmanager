package io.adobe.cloudmanager.impl.network.dns;

import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import com.adobe.aio.ims.feign.AuthInterceptor;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.DomainName;
import io.adobe.cloudmanager.DomainNameApi;
import io.adobe.cloudmanager.DomainNameUpdate;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.impl.AbstractApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class DomainNameTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("network/dns/get.json");
  private static final JsonBody LIST_BODY = loadBodyJson("network/dns/list.json");

  private DomainNameApi underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = DomainNameApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainNames");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown.");
    assertEquals(String.format("Cannot list Domain Names: %s/api/program/1/domainNames (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainNames");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<DomainName> names = underTest.list("1");
    assertEquals(2, names.size(), "List size is correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_limit_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/domainNames")
        .withQueryStringParameter("limit", "10")
        .withQueryStringParameter("start", "0");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1", 10), "Exception thrown.");
    assertEquals(String.format("Cannot list Domain Names: %s/api/program/1/domainNames?start=0&limit=10 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/domainNames")
        .withQueryStringParameter("limit", "10")
        .withQueryStringParameter("start", "0");
    client.when(list).respond(response().withBody(LIST_BODY));
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<DomainName> names = underTest.list("1", 10);
    assertEquals(2, names.size(), "List size is correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_start_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/domainNames")
        .withQueryStringParameter("limit", "10")
        .withQueryStringParameter("start", "10");
    client.when(list).respond(response().withBody(LIST_BODY));
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<DomainName> names = underTest.list("1", 10, 10);
    assertEquals(2, names.size(), "List size is correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void create_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/domainNames")
        .withBody(json("{ \"name\": \"customer.com\", \"environmentId\": 1, \"certificateId\": 1, \"dnsTxtRecord\": \"adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef\", \"dnsZone\": \"customer.com.\" }"));
    client.when(post).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> {
      underTest.create("1", "customer.com", "1", "1", "adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef", "customer.com.");
    }, "Exception thrown.");
    assertEquals(String.format("Cannot create Domain Name: %s/api/program/1/domainNames (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void create_failure_412() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/domainNames")
        .withBody(json("{ \"name\": \"customer.com\", \"environmentId\": 1, \"certificateId\": 1, \"dnsTxtRecord\": \"adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef\", \"dnsZone\": \"customer.com.\" }"));
    client.when(post).respond(response().withStatusCode(PRECONDITION_FAILED_412.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> {
      underTest.create("1", "customer.com", "1", "1", "adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef", "customer.com.");
    }, "Exception thrown.");
    assertEquals("Domain Names are not supported on non-Cloud Service programs.", exception.getMessage(), "Message was correct.");
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
        .withPath("/api/program/1/domainNames")
        .withBody(json("{ \"name\": \"customer.com\", \"environmentId\": 1, \"certificateId\": 1, \"dnsTxtRecord\": \"adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef\", \"dnsZone\": \"customer.com.\" }"));
    client.when(post).respond(response().withBody(GET_BODY));
    DomainName dn = underTest.create("1", "customer.com", "1", "1", "adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef", "customer.com.");
    assertNotNull(dn);
    client.verify(post);
    client.clear(post);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainName/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get Domain Name: %s/api/program/1/domainName/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainName/1");
    client.when(get).respond(response().withBody(GET_BODY));
    assertNotNull(underTest.get("1", "1"));
    client.verify(get);
    client.clear(get);
  }

  @Test
  void update_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/domainName/1")
        .withBody(json("{ \"id\": 1, \"name\": \"customer.com\", \"environmentId\": 1, \"tier\": \"preview\", \"certificateId\": 1, \"dnsTxtRecord\": \"adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef\", \"dnsZone\": \"customer.com.\" }"));
    client.when(put).respond(response().withStatusCode(BAD_REQUEST_400.code()));

    DomainNameUpdate update = DomainNameUpdate.builder()
        .name("customer.com")
        .environmentId("1")
        .tier(Environment.Tier.PREVIEW)
        .certificateId("1")
        .txtRecord("adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef")
        .zone("customer.com.").build();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.update("1", "1", update), "Exception thrown.");
    assertEquals(String.format("Cannot update Domain Name: %s/api/program/1/domainName/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
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
        .withPath("/api/program/1/domainName/1")
        .withBody(json("{ \"id\": 1, \"name\": \"customer.com\", \"environmentId\": 1, \"tier\": \"preview\", \"certificateId\": 1, \"dnsTxtRecord\": \"adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef\", \"dnsZone\": \"customer.com.\" }"));
    client.when(put).respond(response().withBody(GET_BODY));

    DomainNameUpdate update = DomainNameUpdate.builder()
        .name("customer.com")
        .environmentId("1")
        .tier(Environment.Tier.PREVIEW)
        .certificateId("1")
        .txtRecord("adobe-aem-verification=www.adobe.com/1/2/ab-cd-ef")
        .zone("customer.com.").build();
    assertNotNull(underTest.update("1", "1", update));
    client.verify(put);
    client.clear(put);
  }

  @Test
  void delete_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainName/1");
    client.when(del).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.delete("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot delete Domain Name: %s/api/program/1/domainName/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainName/1");
    client.when(del).respond(response().withStatusCode(ACCEPTED_202.code()));
    underTest.delete("1", "1");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void deploy_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainName/1/deploy");
    client.when(post).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deploy("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot deploy Domain Name: %s/api/program/1/domainName/1/deploy (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void deploy_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainName/1/deploy");
    client.when(post).respond(response().withBody(GET_BODY));
    assertNotNull(underTest.deploy("1", "1"));
    client.verify(post);
    client.clear(post);
  }

  @Test
  void verify_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainName/1/verify");
    client.when(post).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.verify("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot verify Domain Name: %s/api/program/1/domainName/1/verify (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void verify_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/domainName/1/verify");
    client.when(post).respond(response().withBody(GET_BODY));
    assertNotNull(underTest.verify("1", "1"));
    client.verify(post);
    client.clear(post);
  }

  @Test
  void validate_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/domainNames/validate")
        .withBody(json("{ \"name\": \"customer.com\", \"environmentId\": 1, \"certificateId\": 1 }"));
    client.when(post).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.validate("1", "customer.com", "1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot validate Domain Name: %s/api/program/1/domainNames/validate (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void validate_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/domainNames/validate")
        .withBody(json("{ \"name\": \"customer.com\", \"environmentId\": 1, \"certificateId\": 1 }"));
    client.when(post).respond(response().withBody(GET_BODY));
    DomainName dn = underTest.validate("1", "customer.com", "1", "1");
    assertNotNull(dn);
    client.verify(post);
    client.clear(post);

  }
}


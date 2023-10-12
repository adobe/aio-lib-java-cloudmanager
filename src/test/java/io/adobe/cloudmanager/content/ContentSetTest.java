package io.adobe.cloudmanager.content;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.adobe.aio.ims.feign.AuthInterceptor;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.ContentFlow;
import io.adobe.cloudmanager.ContentSet;
import io.adobe.cloudmanager.ContentSetApi;
import io.adobe.cloudmanager.impl.AbstractApiTest;
import io.adobe.cloudmanager.impl.content.ContentFlowImpl;
import io.adobe.cloudmanager.impl.content.ContentSetApiImpl;
import io.adobe.cloudmanager.impl.content.ContentSetImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;

import static com.adobe.aio.util.Constants.*;
import static io.adobe.cloudmanager.ContentSet.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class ContentSetTest extends AbstractApiTest {

  private static final JsonBody GET_SET_BODY = loadBodyJson("content/set/get.json");
  public static final JsonBody LIST_SET_BODY = loadBodyJson("content/set/list.json");
  private static final JsonBody GET_FLOW_BODY = loadBodyJson("content/flow/get.json");
  public static final JsonBody LIST_FLOW_BODY = loadBodyJson("content/flow/list.json");

  private ContentSetApiImpl underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = (ContentSetApiImpl) ContentSetApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  @Test
  void list_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSets");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown");
    assertEquals(String.format("Cannot list content sets: %s/api/program/1/contentSets (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSets");
    client.when(get).respond(response().withBody(LIST_SET_BODY));
    List<ContentSet> list = (List<ContentSet>) underTest.list("1");
    assertEquals(2, list.size(), "List correct.");
    ContentSet cs = list.get(0);
    assertEquals(1, cs.getPathDefinitions().size());
    List<PathDefinition> pds = (List<PathDefinition>) cs.getPathDefinitions();
    PathDefinition pd = pds.get(0);
    assertEquals("/content/foo", pd.getPath());
    assertEquals(1, pd.getExcluded().size());

    cs = list.get(1);
    assertEquals(1, cs.getPathDefinitions().size());
    pds = (List<PathDefinition>) cs.getPathDefinitions();
    pd = pds.get(0);
    assertEquals("/content/bar", pd.getPath());
    assertEquals(2, pd.getExcluded().size());

    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_limit_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1", 10), "Exception thrown");
    assertEquals(String.format("Cannot list content sets: %s/api/program/1/contentSets?start=0&limit=10 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(LIST_SET_BODY));
    List<ContentSet> list = (List<ContentSet>) underTest.list("1", 10);
    assertEquals(2, list.size(), "List correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_start_limit_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1", 10, 10), "Exception thrown");
    assertEquals(String.format("Cannot list content sets: %s/api/program/1/contentSets?start=10&limit=10 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_start_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(LIST_SET_BODY));
    List<ContentSet> list = (List<ContentSet>) underTest.list("1", 10, 10);
    assertEquals(2, list.size(), "List correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void create_failure_400() {
    List<PathDefinition> pds = new ArrayList<>();
    Set<String> exclusions = new HashSet<>();
    exclusions.add("/content/foo/bar");
    exclusions.add("/content/foo/foo");
    pds.add(new PathDefinition("/content/foo", exclusions));

    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POSt")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.create("1", "Test", "Description", pds), "Exception thrown.");
    assertEquals(String.format("Cannot create content set: %s/api/program/1/contentSets (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void create_success() throws CloudManagerApiException {
    List<PathDefinition> pds = new ArrayList<>();
    Set<String> exclusions = new HashSet<>();
    exclusions.add("/content/foo/bar");
    exclusions.add("/content/foo/foo");
    pds.add(new PathDefinition("/content/foo", exclusions));

    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(post).respond(response().withBody(GET_SET_BODY));
    ContentSet cs = underTest.create("1", "Test", "Description", pds);
    assertNotNull(cs);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(post);
    client.clear(post);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get content set: %s/api/program/1/contentSet/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));
    ContentSet cs = underTest.get("1", "1");
    assertNotNull(cs);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void update_failure_400() {
    List<PathDefinition> pds = new ArrayList<>();
    Set<String> exclusions = new HashSet<>();
    exclusions.add("/content/foo/bar");
    exclusions.add("/content/foo/foo");
    pds.add(new PathDefinition("/content/foo", exclusions));

    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.update("1", "1", "Test", "Description", pds), "Exception thrown.");
    assertEquals(String.format("Cannot update content set: %s/api/program/1/contentSet/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void update_success_name(@Mock io.adobe.cloudmanager.impl.generated.ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));

    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Updated\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withBody(GET_SET_BODY));

    ContentSet cs = new ContentSetImpl(mock, underTest);
    cs.update("Updated", null, null);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void update_success_description(@Mock io.adobe.cloudmanager.impl.generated.ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));

    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Updated\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withBody(GET_SET_BODY));

    ContentSet cs = new ContentSetImpl(mock, underTest);
    cs.update(null, "Updated", null);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void update_success_paths(@Mock io.adobe.cloudmanager.impl.generated.ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));

    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/not/content/foo\", \"excluded\": [\"/not/content/foo/bar\", \"/not/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withBody(GET_SET_BODY));

    Set<String> exclusions = new HashSet<>();
    exclusions.add("/not/content/foo/bar");
    exclusions.add("/not/content/foo/foo");
    PathDefinition pd = new PathDefinition("/not/content/foo", exclusions);
    List<PathDefinition> paths = new ArrayList<>();
    paths.add(pd);

    ContentSet cs = new ContentSetImpl(mock, underTest);
    cs.update(null, null, paths);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void delete_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(del).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.delete("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot delete content set: %s/api/program/1/contentSet/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_success(@Mock io.adobe.cloudmanager.impl.generated.ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(del).respond(response().withBody(GET_SET_BODY));
    new ContentSetImpl(mock, underTest).delete();
    client.verify(del);
    client.clear(del);
  }

  @Test
  void listFlows_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlows");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listFlows("1"), "Exception thrown");
    assertEquals(String.format("Cannot list content flows: %s/api/program/1/contentFlows (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlows");
    client.when(get).respond(response().withBody(LIST_FLOW_BODY));
    List<ContentFlow> list = (List<ContentFlow>) underTest.listFlows("1");
    assertEquals(2, list.size(), "List correct.");
    ContentFlow cf = list.get(0);
    ContentFlow.Results results = cf.getExportResults();
    assertEquals("0", results.getErrorCode());
    assertEquals("Success", results.getMessage());
    assertEquals("20 Exported", results.getDetails().get(0));
    cf = list.get(1);
    results = cf.getExportResults();
    assertEquals("0", results.getErrorCode());
    assertEquals("Running", results.getMessage());
    assertEquals("5 Exported", results.getDetails().get(0));
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_limit_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listFlows("1", 10), "Exception thrown");
    assertEquals(String.format("Cannot list content flows: %s/api/program/1/contentFlows?start=0&limit=10 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(LIST_FLOW_BODY));
    List<ContentFlow> list = (List<ContentFlow>) underTest.listFlows("1", 10);
    assertEquals(2, list.size(), "List correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_start_limit_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listFlows("1", 10, 10), "Exception thrown");
    assertEquals(String.format("Cannot list content flows: %s/api/program/1/contentFlows?start=10&limit=10 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_start_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(LIST_FLOW_BODY));
    List<ContentFlow> list = (List<ContentFlow>) underTest.listFlows("1", 10, 10);
    assertEquals(2, list.size(), "List correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void startFlow_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/contentFlow")
        .withBody(json("{ \"contentSetId\": \"1\", \"destEnvironmentId\": \"2\", \"tier\": \"author\", \"includeACL\": true, \"destProgramId\": \"1\" }"));
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startFlow("1", "1", "1", "2", true), "Exception thrown.");
    assertEquals(String.format("Cannot start content flow: %s/api/program/1/environment/1/contentFlow (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void startFlow_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/contentFlow")
        .withBody(json("{ \"contentSetId\": \"1\", \"destEnvironmentId\": \"2\", \"tier\": \"author\", \"includeACL\": true, \"destProgramId\": \"1\" }"));
    client.when(post).respond(response().withStatusCode(FORBIDDEN_403.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startFlow("1", "1", "1", "2", true), "Exception thrown.");
    assertEquals(String.format("Cannot start content flow: %s/api/program/1/environment/1/contentFlow (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void startFlow_success(@Mock io.adobe.cloudmanager.impl.generated.ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/contentFlow")
        .withBody(json("{ \"contentSetId\": \"1\", \"destEnvironmentId\": \"2\", \"tier\": \"author\", \"includeACL\": false, \"destProgramId\": \"1\" }"));
    client.when(post).respond(response().withBody(GET_FLOW_BODY));
    ContentFlow cf = new ContentSetImpl(mock, underTest).startFlow("1", "2", false);
    assertNotNull(cf);
    client.verify(post);
    client.clear(post);
  }

  @Test
  void getFlow_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlow/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getFlow("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get content flow: %s/api/program/1/contentFlow/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getFlow_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlow/1");
    client.when(get).respond(response().withBody(GET_FLOW_BODY));
    ContentFlow cf = underTest.getFlow("1", "1");
    assertNotNull(cf);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void cancelFlow_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlow/1");
    client.when(del).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelFlow("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot cancel content flow: %s/api/program/1/contentFlow/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void cancelFlow_success(@Mock io.adobe.cloudmanager.impl.generated.ContentFlow mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getDestProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlow/1");
    client.when(del).respond(response().withBody(GET_FLOW_BODY));
    new ContentFlowImpl(mock, underTest).cancel();
    client.verify(del);
    client.clear(del);
  }

}

package io.adobe.cloudmanager.impl.environment;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.adobe.aio.ims.feign.AuthInterceptor;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.EnvironmentApi;
import io.adobe.cloudmanager.EnvironmentLog;
import io.adobe.cloudmanager.LogOption;
import io.adobe.cloudmanager.Region;
import io.adobe.cloudmanager.RegionDeployment;
import io.adobe.cloudmanager.Variable;
import io.adobe.cloudmanager.impl.AbstractApiTest;
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

public class EnvironmentTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("environment/get.json");
  private static final JsonBody LIST_BODY = loadBodyJson("environment/list.json");
  private static final JsonBody LIST_DEV_BODY = loadBodyJson("environment/list-dev.json");
  private static final JsonBody LIST_LOGS_BODY = loadBodyJson("environment/list-logs.json");
  public static final JsonBody LIST_REGIONS_BODY = loadBodyJson("environment/list-regions.json");
  public static final JsonBody GET_REGION_BODY = loadBodyJson("environment/get-region.json");
  public static final JsonBody LIST_VARIABLES_BODY = loadBodyJson("environment/list-variables.json");

  private EnvironmentApi underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = EnvironmentApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown.");
    assertEquals(String.format("Cannot list environments: %s/api/program/1/environments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<Environment> environments = underTest.list("1");
    assertEquals(4, environments.size(), "List was correct size.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_type_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environments")
        .withQueryStringParameter("type", "dev");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1", Environment.Type.DEV), "Exception thrown.");
    assertEquals(String.format("Cannot list environments: %s/api/program/1/environments?type=dev (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_type_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environments")
        .withQueryStringParameter("type", "dev");
    client.when(list).respond(response().withBody(LIST_DEV_BODY));

    Collection<Environment> environments = underTest.list("1", Environment.Type.DEV);
    assertEquals(2, environments.size(), "List was correct size.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void create_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environments")
        .withBody(json("{ \"name\": \"Test\", \"type\": \"dev\", \"region\": \"va7\" }"));
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.create("1", "Test", Environment.Type.DEV, "va7", null),
        "Exception thrown.");
    assertEquals(String.format("Cannot create environment: %s/api/program/1/environments (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void create_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request().withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environments")
        .withBody(json("{ \"name\": \"Test\", \"type\": \"dev\", \"region\": \"va7\", \"description\":  \"desc\" }"));
    client.when(post).respond(response().withBody(GET_BODY));

    assertNotNull(underTest.create("1", "Test", Environment.Type.DEV, "va7", "desc"), "Creation successful.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get environment: %s/api/program/1/environment/1 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1");
    client.when(get).respond(response().withBody(GET_BODY));
    assertNotNull(underTest.get("1", "1"));
    client.verify(get);
    client.clear(get);
  }

  @Test
  void delete_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request()
        .withMethod("DELETE")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1")
        .withQueryStringParameter("ignoreResourcesDeletionResult", "false");
    client.when(del).respond(response().withStatusCode(BAD_REQUEST_400.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.delete("1", "1"), "Exception thrown.");
    assertEquals("Cannot delete environment, deletion in progress.", exception.getMessage(), "Message was correct.");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_success(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest del = request()
        .withMethod("DELETE")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1")
        .withQueryStringParameter("ignoreResourcesDeletionResult", "false");
    client.when(del).respond(response().withBody(GET_BODY));

    underTest.delete(new EnvironmentImpl(mock, underTest));
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_success_via_environment(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest del = request()
        .withMethod("DELETE")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1")
        .withQueryStringParameter("ignoreResourcesDeletionResult", "false");
    client.when(del).respond(response().withBody(GET_BODY));

    new EnvironmentImpl(mock, underTest).delete();
    client.verify(del);
    client.clear(del);
  }

  @Test
  void list_logs_failure_400(@Mock LogOption option) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(option.getService()).thenReturn("author");
    when(option.getName()).thenReturn("aemerror");
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(list).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listLogs("1", "1", option, 1), "Exception thrown.");
    assertEquals(String.format("Cannot get logs: %s/api/program/1/environment/1/logs?service=author&name=aemerror&days=1 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_logs_success(@Mock LogOption option, @Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(option.getService()).thenReturn("author");
    when(option.getName()).thenReturn("aemerror");
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(list).respond(response().withBody(LIST_LOGS_BODY));
    Collection<EnvironmentLog> logs = new EnvironmentImpl(mock, underTest).listLogs(option, 1);
    assertEquals(2, logs.size(), "Log file count correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void getLogDownloadUrl_failure_400(@Mock LogOption option) {
    LocalDate date = LocalDate.now();
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(option.getService()).thenReturn("author");
    when(option.getName()).thenReturn("aemerror");
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("date", date.toString());
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getLogDownloadUrl("1", "1", option, date), "Exception thrown.");
    assertEquals(String.format("Cannot get logs: %s/api/program/1/environment/1/logs/download?service=author&name=aemerror&date=%s (400 Bad Request).", baseUrl, date), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getLogDownloadUrl_no_redirect(@Mock LogOption option) {
    LocalDate date = LocalDate.now();
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(option.getService()).thenReturn("author");
    when(option.getName()).thenReturn("aemerror");
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("date", date.toString());
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody("{}"));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getLogDownloadUrl("1", "1", option, date), "Exception thrown.");
    assertEquals(String.format("Log redirect for environment 1, service 'author', log name 'aemerror', date '%s' did not exist.", date), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getLogDownloadUrl_success(@Mock LogOption option, @Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    LocalDate date = LocalDate.now();
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(option.getService()).thenReturn("author");
    when(option.getName()).thenReturn("aemerror");
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("date", date.toString());
    client.when(get).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/author-aemerror-%s.txt\" }", baseUrl, date))));
    String url = new EnvironmentImpl(mock, underTest).getLogDownloadUrl(option, date);
    assertEquals(String.format("%s/logs/author-aemerror-%s.txt", baseUrl, date), url, "URL was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getRegionDeployment_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1/regionDeployments/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getRegionDeployment("1", "1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get region deployment: %s/api/program/1/environment/1/regionDeployments/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getRegionDeployment_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1/regionDeployments/1");
    client.when(get).respond(response().withBody(GET_REGION_BODY));

    assertNotNull(underTest.getRegionDeployment("1", "1", "1"));
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listRegionDeployments_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1/regionDeployments");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listRegionDeployments("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot list region deployments: %s/api/program/1/environment/1/regionDeployments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listsRegionDeployments_success(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1/regionDeployments");
    client.when(list).respond(response().withBody(LIST_REGIONS_BODY));

    Collection<RegionDeployment> deployments = new EnvironmentImpl(mock, underTest).listRegionDeployments();
    assertEquals(2, deployments.size(), "Correct list size.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void createRegionDeployment_failure_400(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments")
        .withBody(json("[\"can2\", \"gbr9\"]"));
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    Environment env = new EnvironmentImpl(mock, underTest);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.createRegionDeployments(env, Region.CANADA, Region.SOUTH_UK), "Exception thrown.");
    assertEquals(String.format("Cannot add region deployments: %s/api/program/1/environment/1/regionDeployments (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void createRegionDeployment_success_via_environment(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments")
        .withBody(json("[\"can2\"]"));
    client.when(post).respond(response().withBody(LIST_REGIONS_BODY));
    new EnvironmentImpl(mock, underTest).addRegionDeployment(Region.CANADA);
    client.verify(post);
    client.clear(post);
  }

  @Test
  void createRegionDeployment_success(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments")
        .withBody(json("[\"can2\", \"gbr9\"]"));
    client.when(post).respond(response().withBody(LIST_REGIONS_BODY));
    Environment env = new EnvironmentImpl(mock, underTest);
    underTest.createRegionDeployments(env, Region.CANADA, Region.SOUTH_UK);
    client.verify(post);
    client.clear(post);
  }

  @Test
  void removeRegionDeployment_failure_no_deployment(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments");
    client.when(list).respond(response().withBody(LIST_REGIONS_BODY));

    Environment env = new EnvironmentImpl(mock, underTest);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.removeRegionDeployments(env, Region.CANADA), "Exception thrown.");
    assertEquals("Cannot remove region deployment, Environment 1 is not deployed to region 'can2'.", exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void removeRegionDeployment_failure_400(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments");
    client.when(list).respond(response().withBody(LIST_REGIONS_BODY));
    HttpRequest post = request()
        .withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments")
        .withBody(json("[ { \"id\": \"2\", \"status\": \"TO_DELETE\" } ]"));
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    Environment env = new EnvironmentImpl(mock, underTest);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.removeRegionDeployments(env, Region.SOUTH_UK), "Exception thrown.");
    assertEquals(String.format("Cannot remove region deployments: %s/api/program/1/environment/1/regionDeployments (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list, post);
    client.clear(list);
    client.clear(post);
  }

  @Test
  void removeRegionDeployment_success(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments");
    client.when(list).respond(response().withBody(LIST_REGIONS_BODY));
    HttpRequest post = request()
        .withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments")
        .withBody(json("[ { \"id\": \"1\", \"status\": \"TO_DELETE\" }, { \"id\": \"2\", \"status\": \"TO_DELETE\" } ]"));
    client.when(post).respond(response().withBody(LIST_REGIONS_BODY));
    Environment env = new EnvironmentImpl(mock, underTest);
    underTest.removeRegionDeployments(env, Region.SOUTH_UK, Region.EAST_US);
    client.verify(list, post);
    client.clear(list);
    client.clear(post);
  }

  @Test
  void removeRegionDeployment_success_via_environment(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments");
    client.when(list).respond(response().withBody(LIST_REGIONS_BODY));
    HttpRequest post = request()
        .withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments")
        .withBody(json("[ { \"id\": \"2\", \"status\": \"TO_DELETE\" } ]"));
    client.when(post).respond(response().withBody(LIST_REGIONS_BODY));
    new EnvironmentImpl(mock, underTest).removeRegionDeployment(Region.SOUTH_UK);
    client.verify(list, post);
    client.clear(list);
    client.clear(post);
  }

  @Test
  void listVariables_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1/variables");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getVariables("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot list environment variables: %s/api/program/1/environment/1/variables (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listVariables_success(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1/variables");
    client.when(list).respond(response().withBody(LIST_VARIABLES_BODY));
    Set<Variable> variables = new EnvironmentImpl(mock, underTest).getVariables();
    assertEquals(2, variables.size(), "Correct response");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void setVariables_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest patch = request().withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withHeader("Content-Type", "application/json")
        .withPath("/api/program/1/environment/1/variables")
        .withBody(json("[ { " +
            "\"name\": \"foo\", \"value\": \"bar\", \"type\": \"string\", \"service\": \"author\" }, " +
            "{ \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\", \"service\": \"publish\" " +
            "} ]"));
    client.when(patch).respond(response().withStatusCode(NOT_FOUND_404.code()));
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).service("author").build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).service("publish").build();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setVariables("1", "1", var1, var2), "Exception thrown.");
    assertEquals(String.format("Cannot set environment variables: %s/api/program/1/environment/1/variables (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(patch);
    client.clear(patch);
  }

  @Test
  void setVariables_failure_404(@Mock io.adobe.cloudmanager.impl.generated.Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest patch = request().withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withHeader("Content-Type", "application/json")
        .withPath("/api/program/1/environment/1/variables")
        .withBody(json("[ { " +
            "\"name\": \"foo\", \"value\": \"bar\", \"type\": \"string\", \"service\": \"author\" }, " +
            "{ \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\", \"service\": \"publish\" " +
            "} ]"));
    client.when(patch).respond(response().withBody(LIST_VARIABLES_BODY));
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).service("author").build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).service("publish").build();

    Set<Variable> variables = new EnvironmentImpl(mock, underTest).setVariables(var1, var2);
    assertEquals(2, variables.size(), "Correct response");
    client.verify(patch);
    client.clear(patch);
  }
}

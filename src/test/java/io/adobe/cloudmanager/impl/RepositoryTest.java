package io.adobe.cloudmanager.impl;

import java.util.Collection;
import java.util.UUID;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.Repository;
import io.adobe.cloudmanager.impl.generated.EmbeddedProgram;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;
import org.mockserver.verify.VerificationTimes;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;

public class RepositoryTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("repository/get.json");
  public static final JsonBody LIST_BODY = loadBodyJson("repository/list.json");


  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repositories");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listRepositories("1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve repositories: %s/api/program/1/repositories (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repositories");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<Repository> repositories = underTest.listRepositories("1");
    assertEquals(3, repositories.size());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success_viaProgram(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repositories");
    client.when(list).respond(response().withBody(LIST_BODY));

    Program program = new ProgramImpl(mock, underTest);
    Collection<Repository> repositories = program.listRepositories();
    assertEquals(3, repositories.size());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_with_limit_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/repositories")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listRepositories("1", 10), "Exception thrown.");
    assertTrue(exception.getMessage().contains("(404 Not Found)."), "Message was correct");
    assertTrue(exception.getMessage().contains("start=0"), "Message was correct");
    assertTrue(exception.getMessage().contains("limit=10"), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_with_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/repositories")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<Repository> repositories = underTest.listRepositories("1", 10);
    assertEquals(3, repositories.size());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_with_start_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/repositories")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<Repository> repositories = underTest.listRepositories("1", 10, 10);
    assertEquals(3, repositories.size());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repository/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getRepository("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve repository: %s/api/program/1/repository/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repository/1");
    client.when(get).respond(response().withBody(GET_BODY));

    Repository repository = underTest.getRepository("1", "1");
    assertNotNull(repository, "Repository retrieval success.");
    assertEquals("1", repository.getId(), "Id was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listBranches_failure_404(@Mock Repository mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repository/1/branches");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listBranches(mock), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve repository branches: %s/api/program/1/repository/1/branches (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listBranches_success(@Mock Repository mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repository/1/branches");
    client.when(list).respond(response().withBody(loadBodyJson("repository/branches.json")));

    Collection<String> branches = underTest.listBranches(mock);
    assertEquals(1, branches.size());
    assertEquals("main", branches.stream().findFirst().orElseThrow());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listBranches_success_viaRepository(@Mock io.adobe.cloudmanager.impl.generated.Repository mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repository/1/branches");
    client.when(list).respond(response().withBody(loadBodyJson("repository/branches.json")));

    Repository repository = new RepositoryImpl(mock, underTest);
    Collection<String> branches = repository.listBranches();
    assertEquals(1, branches.size());
    assertEquals("main", branches.stream().findFirst().orElseThrow());
    client.verify(list);
    client.clear(list);
  }
}

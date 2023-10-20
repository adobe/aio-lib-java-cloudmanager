package io.adobe.cloudmanager.impl.repository;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import com.adobe.aio.ims.feign.AuthInterceptor;
import io.adobe.cloudmanager.ApiBuilder;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.Repository;
import io.adobe.cloudmanager.RepositoryApi;
import io.adobe.cloudmanager.impl.AbstractApiTest;
import io.adobe.cloudmanager.impl.generated.EmbeddedProgram;
import io.adobe.cloudmanager.impl.program.ProgramImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;
import org.mockserver.verify.VerificationTimes;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class RepositoryTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("repository/get.json");
  public static final JsonBody LIST_BODY = loadBodyJson("repository/list.json");

  private RepositoryApi underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = new ApiBuilder<>(RepositoryApi.class).workspace(workspace).url(new URL(baseUrl)).build();
    }
  }
  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repositories");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve repositories: %s/api/program/1/repositories (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }


  @Test
  void list_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repositories");

    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"repositories\": [] } }")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);
  }


  @Test
  void list_success(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repositories");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<Repository> repositories = underTest.list(new ProgramImpl(mock, null));
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1", 10), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve repositories: %s/api/program/1/repositories?start=0&limit=10 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_limit_success(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");

    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/repositories")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(list).respond(response().withBody(LIST_BODY));

    Program program = new ProgramImpl(mock, null);
    Collection<Repository> repositories = underTest.list(program, 10);
    assertEquals(3, repositories.size());
    client.verify(list);
    client.clear(list);
  }


  @Test
  void list_start_limit_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/repositories")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");

    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(underTest.list("1", 10, 10).isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.list("1", 10, 10).isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"repositories\": [] } }")));
    assertTrue(underTest.list("1", 10, 10).isEmpty());
    client.verify(list);
    client.clear(list);
  }


  @Test
  void list_start_limit_success(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");

    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/repositories")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(list).respond(response().withBody(LIST_BODY));

    Program program = new ProgramImpl(mock, null);
    Collection<Repository> repositories = underTest.list(program, 10, 10);
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve repository: %s/api/program/1/repository/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void get_success(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repository/1");
    client.when(get).respond(response().withBody(GET_BODY));

    Program program = new ProgramImpl(mock, null);
    Repository repository = underTest.get(program, "1");
    assertNotNull(repository, "Repository retrieval success.");
    assertEquals("1", repository.getId(), "Id was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listBranches_failure_404(@Mock Repository mock) {
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
  void listBranches_empty(@Mock Repository mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/repository/1/branches");

    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(underTest.listBranches(mock).isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.listBranches(mock).isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"branches\": [] } }")));
    assertTrue(underTest.listBranches(mock).isEmpty());
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

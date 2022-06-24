package com.adobe.aio.cloudmanager.feign;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
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

import java.util.Collection;
import java.util.UUID;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Program;
import com.adobe.aio.cloudmanager.Repository;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.verify.VerificationTimes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;

public class RepositoryApiTest extends AbstractApiClientTest {

  @Test
  void list_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/repositories");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listRepositories("1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve repositories: %s/api/program/1/repositories (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
  }
  
  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest getProgram = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(getProgram).respond(response().withBody(loadBodyJson("program/get.json")));

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/repositories");
    client.when(list).respond(response().withBody(loadBodyJson("repository/list.json")));

    Program program = underTest.getProgram("1");
    Collection<Repository> repositories = underTest.listRepositories(program);
    assertEquals(3, repositories.size());
    client.clear(getProgram);
    client.clear(list);
  }

  @Test
  void list_with_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest getProgram = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(getProgram).respond(response().withBody(loadBodyJson("program/get.json")));

    HttpRequest list = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/repositories")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "20");
    client.when(list).respond(response().withBody(loadBodyJson("repository/list.json")));

    Program program = underTest.getProgram("1");
    Collection<Repository> repositories = underTest.listRepositories(program, 20);
    assertEquals(3, repositories.size());
    client.clear(getProgram);
    client.clear(list);
  }

  @Test
  void list_with_start_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest getProgram = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(getProgram).respond(response().withBody(loadBodyJson("program/get.json")));

    HttpRequest list = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/repositories")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "20");
    client.when(list).respond(response().withBody(loadBodyJson("repository/list.json")));

    Program program = underTest.getProgram("1");
    Collection<Repository> repositories = underTest.listRepositories(program, 10, 20);
    assertEquals(3, repositories.size());
    client.clear(getProgram);
    client.clear(list);
  }

  @Test
  void get_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/repository/1");
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

    HttpRequest getProgram = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(getProgram).respond(response().withBody(loadBodyJson("program/get.json")));
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/repository/1");
    client.when(get).respond(response().withBody(loadBodyJson("repository/get.json")));

    Program program = underTest.getProgram("1");
    Repository repository = underTest.getRepository(program, "1");
    assertNotNull(repository, "Repository retrieval success.");
    assertEquals("1", repository.getId(), "Id was correct.");
    client.clear(getProgram);
    client.clear(get);
  }
  
  @Test
  void listBranches_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest repo  = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/repository/1");
    client.when(repo).respond(response().withBody(loadBodyJson("repository/get.json")));

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/repository/1/branches");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    
    Repository repository = underTest.getRepository("1", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listBranches(repository), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve repository branches: %s/api/program/1/repository/1/branches (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get, VerificationTimes.exactly(1));
    client.clear(repo);
    client.clear(get);
  }

  @Test
  void listBranches_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest repo  = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/repository/1");
    client.when(repo).respond(response().withBody(loadBodyJson("repository/get.json")));

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/repository/1/branches");
    client.when(get).respond(response().withBody(loadBodyJson("repository/branches.json")));

    Repository repository = underTest.getRepository("1", "1");
    Collection<String> branches = underTest.listBranches(repository);
    assertEquals(1, branches.size());
    assertEquals("main", branches.stream().findFirst().get());
    client.clear(repo);
    client.clear(get);
  }
}

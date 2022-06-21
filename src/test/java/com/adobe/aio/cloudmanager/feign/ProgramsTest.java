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
import org.junit.jupiter.api.Test;
import org.mockserver.model.ClearType;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class ProgramsTest extends AbstractApiClientTest {

  @Test
  void list_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/programs");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listPrograms, "Exception thrown.");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
  }

  @Test
  void list_failure403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/programs");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"error_code\":\"1234\", \"message\":\"some message\" }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listPrograms, "Exception thrown.");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden) - Detail: some message (Code: 1234).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(list);
  }

  @Test
  void list_failure403_errorMessageOnly() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/programs");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"message\":\"some message\" }")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listPrograms, "Exception thrown.");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden) - Detail: some message.", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(list);
  }

  @Test
  void list_failure403_errorCodeOnly() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/programs");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"error_code\":\"1234\" }")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listPrograms, "Exception thrown.");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(list);
  }

  @Test
  void list_successEmpty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/programs");
    client.when(list).respond(response().withContentType(MediaType.APPLICATION_JSON).withBody("{}"));
    Collection<Program> programs = underTest.listPrograms();
    assertTrue(programs.isEmpty(), "Empty body returns zero length list");
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/programs");
    client.when(list).respond(response().withBody(loadBodyJson("programs/list.json")));

    Collection<Program> programs = underTest.listPrograms();
    assertEquals(7, programs.size(), "Correct length of program list");
    client.clear(list);
  }

  @Test
  void get_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getProgram("1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve program: %s/api/program/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(get).respond(response());
    Program program = underTest.getProgram("1");
    assertNotNull(program, "Program retrieval success.");
    client.clear(get);
  }
  
  @Test
  void delete_failure400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(delete).respond(response().withStatusCode(BAD_REQUEST_400.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteProgram("1"), "Exception was thrown");
    assertEquals(String.format("Cannot delete program: %s/api/program/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete, ClearType.LOG);
  }

  @Test
  void delete_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(delete).respond(response().withStatusCode(ACCEPTED_202.code()));
    
    underTest.deleteProgram("1");
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete, ClearType.LOG);
  }

  @Test
  void delete_viaProgram() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/programs");
    client.when(list).respond(response().withBody(loadBodyJson("programs/list.json")));

    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1");
    client.when(delete).respond(response().withStatusCode(ACCEPTED_202.code()));

    Collection<Program> programs = underTest.listPrograms();
    Program program = programs.stream().filter(p -> p.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    program.delete();
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(list);
    client.clear(delete);
  }
  
  @Test
  void listByTenant_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/tenant/1/programs");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPrograms("1"), "Exception was thrown");
    assertEquals(String.format("Cannot retrieve programs: %s/api/tenant/1/programs (404 Not Found).", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get, ClearType.LOG);
  }
  
  @Test
  void listByTenant_successEmpty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/tenant/1/programs");
    client.when(list).respond(response().withContentType(MediaType.APPLICATION_JSON).withBody("{}"));
    Collection<Program> programs = underTest.listPrograms("1");
    assertTrue(programs.isEmpty(), "Empty body returns zero length list");
    client.clear(list);
  }

  @Test
  void listByTenant_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/tenant/1/programs");
    client.when(list).respond(response().withBody(loadBodyJson("programs/list.json")));

    Collection<Program> programs = underTest.listPrograms("1");
    assertEquals(7, programs.size(), "Correct length of program list");
    client.clear(list);
  }
}

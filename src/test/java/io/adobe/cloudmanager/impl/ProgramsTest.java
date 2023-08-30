package io.adobe.cloudmanager.impl;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2021 Adobe Inc.
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

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.Tenant;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockserver.model.HttpRequest;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;

class ProgramsTest extends AbstractApiTest {

  @Test
  void get_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getProgram("1"), "Exception was thrown");
    assertEquals(String.format("Cannot retrieve program: %s/api/program/1 (404 Not Found).", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1");
    client.when(get).respond(response().withBody(loadBodyJson("programs/get.json")));
    assertNotNull(underTest.getProgram("1"), "Program found.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void delete_failure() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1");
    client.when(del).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteProgram("1"), "Exception was thrown");
    assertEquals(String.format("Cannot delete program: %s/api/program/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1");
    client.when(del).respond(response().withStatusCode(ACCEPTED_202.code()));
    underTest.deleteProgram("1");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_viaProgram(@Mock io.adobe.cloudmanager.impl.generated.EmbeddedProgram mock) throws CloudManagerApiException {
    when(mock.getId()).thenReturn("4");
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/4");
    client.when(del).respond(response().withStatusCode(ACCEPTED_202.code()));
    Program program = new ProgramImpl(mock, underTest);
    program.delete();
    client.verify(del);
    client.clear(del);
  }

  @Test
  void list_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1/programs");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPrograms("1"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/tenant/1/programs (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_successEmpty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1/programs");
    client.when(list).respond(response(). withBody("{}"));
    Collection<Program> programs = underTest.listPrograms("1");
    assertTrue(programs.isEmpty(), "Empty body returns zero length list");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1/programs");
    client.when(list).respond(response(). withBody(loadBodyJson("programs/list.json")));
    Collection<Program> programs = underTest.listPrograms("1");
    assertEquals(7, programs.size(), "Correct length of program list");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_fromTenant(@Mock io.adobe.cloudmanager.impl.generated.Tenant mock) throws CloudManagerApiException {
    when(mock.getId()).thenReturn("1");
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1/programs");
    client.when(list).respond(response(). withBody(loadBodyJson("programs/list.json")));
    Tenant tenant = new TenantImpl(mock, underTest);
    Collection<Program> programs = tenant.listPrograms();
    assertEquals(7, programs.size(), "Correct length of program list");
    client.verify(list);
    client.clear(list);
  }
}

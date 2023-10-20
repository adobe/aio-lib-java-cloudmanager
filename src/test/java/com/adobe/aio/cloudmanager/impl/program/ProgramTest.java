package com.adobe.aio.cloudmanager.impl.program;

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
import com.adobe.aio.cloudmanager.ApiBuilder;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Program;
import com.adobe.aio.cloudmanager.ProgramApi;
import com.adobe.aio.cloudmanager.Region;
import com.adobe.aio.cloudmanager.impl.AbstractApiTest;
import com.adobe.aio.cloudmanager.impl.generated.EmbeddedProgram;
import com.adobe.aio.cloudmanager.impl.generated.Tenant;
import com.adobe.aio.cloudmanager.impl.tenant.TenantImpl;
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

class ProgramTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("program/get.json");
  public static final JsonBody LIST_BODY = loadBodyJson("program/list.json");

  private ProgramApi underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = new ApiBuilder<>(ProgramApi.class).workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1"), "Exception was thrown");
    assertEquals(String.format("Cannot retrieve program: %s/api/program/1 (404 Not Found).", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1");
    client.when(get).respond(response().withBody(GET_BODY));
    assertNotNull(underTest.get("1"), "Program found.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void delete_failure() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1");
    client.when(del).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.delete("1"), "Exception was thrown");
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
    underTest.delete("1");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_via_program(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
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
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1/programs");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/tenant/1/programs (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1/programs");

    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);


    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"programs\": [] } }")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success(@Mock Tenant mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1/programs");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<Program> programs = underTest.list(new TenantImpl(mock));
    assertEquals(7, programs.size(), "Correct length of program list");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listRegions_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/regions");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listRegions("1"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve program regions: %s/api/program/1/regions (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }


  @Test
  void listRegions_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/regions");
    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(underTest.listRegions("1").isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.listRegions("1").isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"regions\": [] } }")));
    assertTrue(underTest.listRegions("1").isEmpty());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listRegions_success(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/regions");
    client.when(list).respond(response().withBody(loadBodyJson("program/regions.json")));
    Collection<Region> regions = new ProgramImpl(mock, underTest).listRegions();
    assertEquals(3, regions.size());
    client.verify(list);
    client.clear(list);
  }
}

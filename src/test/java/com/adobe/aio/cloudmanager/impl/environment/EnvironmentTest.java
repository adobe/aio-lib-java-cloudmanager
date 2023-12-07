package com.adobe.aio.cloudmanager.impl.environment;

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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.adobe.aio.cloudmanager.impl.generated.Environment;
import com.adobe.aio.ims.feign.AuthInterceptor;
import com.adobe.aio.cloudmanager.ApiBuilder;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.EnvironmentApi;
import com.adobe.aio.cloudmanager.EnvironmentLog;
import com.adobe.aio.cloudmanager.LogOption;
import com.adobe.aio.cloudmanager.Region;
import com.adobe.aio.cloudmanager.RegionDeployment;
import com.adobe.aio.cloudmanager.Variable;
import com.adobe.aio.cloudmanager.impl.AbstractApiTest;
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

public class EnvironmentTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("environment/get.json");
  private static final JsonBody LIST_BODY = loadBodyJson("environment/list.json");
  private static final JsonBody LIST_DEV_BODY = loadBodyJson("environment/list-dev.json");
  private static final JsonBody LIST_LOGS_BODY = loadBodyJson("environment/list-logs.json");
  public static final JsonBody LIST_REGIONS_BODY = loadBodyJson("environment/list-regions.json");
  public static final JsonBody GET_REGION_BODY = loadBodyJson("environment/get-region.json");
  public static final JsonBody LIST_VARIABLES_BODY = loadBodyJson("environment/list-variables.json");

  private EnvironmentApi underTest;

  private final LogOption option = LogOption.builder().service("author").name("aemerror").build();

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = new ApiBuilder<>(EnvironmentApi.class).workspace(workspace).url(new URL(baseUrl)).build();
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
  void list_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environments");

    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"embedded\": { \"environments\": [] } }")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<com.adobe.aio.cloudmanager.Environment> environments = underTest.list("1");
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1", com.adobe.aio.cloudmanager.Environment.Type.DEV), "Exception thrown.");
    assertEquals(String.format("Cannot list environments: %s/api/program/1/environments?type=dev (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_type_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environments").withQueryStringParameter("type", "dev");

    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(underTest.list("1", com.adobe.aio.cloudmanager.Environment.Type.DEV).isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.list("1", com.adobe.aio.cloudmanager.Environment.Type.DEV).isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"embedded\": { \"environments\": [] } }")));
    assertTrue(underTest.list("1", com.adobe.aio.cloudmanager.Environment.Type.DEV).isEmpty());
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

    Collection<com.adobe.aio.cloudmanager.Environment> environments = underTest.list("1", com.adobe.aio.cloudmanager.Environment.Type.DEV);
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
        () -> underTest.create("1", "Test", com.adobe.aio.cloudmanager.Environment.Type.DEV, "va7", null),
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

    assertNotNull(underTest.create("1", "Test", com.adobe.aio.cloudmanager.Environment.Type.DEV, "va7", "desc"), "Creation successful.");
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
  void delete_success(@Mock Environment mock) throws CloudManagerApiException {
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

    underTest.delete("1", "1");
    underTest.delete(new EnvironmentImpl(mock, underTest));
    client.verify(del, VerificationTimes.exactly(2));
    client.clear(del);
  }

  @Test
  void delete_success_via_environment(@Mock Environment mock) throws CloudManagerApiException {
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
  void list_logs_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
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
  void list_logs_success(@Mock Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
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
  void getLogDownloadUrl_failure_400() {
    LocalDate date = LocalDate.now();
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
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
  void getLogDownloadUrl_no_redirect() {
    LocalDate date = LocalDate.now();
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("date", date.toString());
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getLogDownloadUrl("1", "1", option, date), "Exception thrown.");
    assertEquals(String.format("Log redirect for environment 1, service 'author', log name 'aemerror', date '%s' did not exist.", date), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getLogDownloadUrl_success(@Mock Environment mock) throws CloudManagerApiException {
    LocalDate date = LocalDate.now();
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
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
  void listsRegionDeployments_empty(@Mock Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    com.adobe.aio.cloudmanager.Environment environment = new EnvironmentImpl(mock, underTest);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1/regionDeployments");

    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(environment.listRegionDeployments().isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(environment.listRegionDeployments().isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"regionDeployments\": [] } }")));
    assertTrue(environment.listRegionDeployments().isEmpty());
    client.verify(list);
    client.clear(list);
  }


  @Test
  void listsRegionDeployments_success(@Mock Environment mock) throws CloudManagerApiException {
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
  void createRegionDeployment_failure_400(@Mock Environment mock) {
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
    com.adobe.aio.cloudmanager.Environment env = new EnvironmentImpl(mock, underTest);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.createRegionDeployments(env, Region.CANADA, Region.SOUTH_UK), "Exception thrown.");
    assertEquals(String.format("Cannot add region deployments: %s/api/program/1/environment/1/regionDeployments (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void createRegionDeployment_success_via_environment(@Mock Environment mock) throws CloudManagerApiException {
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
  void createRegionDeployment_success(@Mock Environment mock) throws CloudManagerApiException {
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
    com.adobe.aio.cloudmanager.Environment env = new EnvironmentImpl(mock, underTest);
    underTest.createRegionDeployments(env, Region.CANADA, Region.SOUTH_UK);
    client.verify(post);
    client.clear(post);
  }

  @Test
  void removeRegionDeployment_failure_no_deployment(@Mock Environment mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/regionDeployments");
    client.when(list).respond(response().withBody(LIST_REGIONS_BODY));

    com.adobe.aio.cloudmanager.Environment env = new EnvironmentImpl(mock, underTest);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.removeRegionDeployments(env, Region.CANADA), "Exception thrown.");
    assertEquals("Cannot remove region deployment, Environment 1 is not deployed to region 'can2'.", exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void removeRegionDeployment_failure_400(@Mock Environment mock) {
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
    com.adobe.aio.cloudmanager.Environment env = new EnvironmentImpl(mock, underTest);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.removeRegionDeployments(env, Region.SOUTH_UK), "Exception thrown.");
    assertEquals(String.format("Cannot remove region deployments: %s/api/program/1/environment/1/regionDeployments (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list, post);
    client.clear(list);
    client.clear(post);
  }

  @Test
  void removeRegionDeployment_success(@Mock Environment mock) throws CloudManagerApiException {
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
    com.adobe.aio.cloudmanager.Environment env = new EnvironmentImpl(mock, underTest);
    underTest.removeRegionDeployments(env, Region.SOUTH_UK, Region.EAST_US);
    client.verify(list, post);
    client.clear(list);
    client.clear(post);
  }

  @Test
  void removeRegionDeployment_success_via_environment(@Mock Environment mock) throws CloudManagerApiException {
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
  void resetRde_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/reset");
    client.when(put).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.resetRde("1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot reset rapid development environment: %s/api/program/1/environment/1/reset (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(put);
    client.clear(put);
  }

  @Test
  void resetRde_success(@Mock Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    when(mock.getType()).thenReturn(Environment.TypeEnum.RDE);
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/reset");
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));
    new EnvironmentImpl(mock, underTest).reset();
    client.verify(put);
    client.clear(put);
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
  void listVariables_empty(@Mock Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    com.adobe.aio.cloudmanager.Environment environment = new EnvironmentImpl(mock, underTest);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environment/1/variables");

    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(environment.getVariables().isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(environment.getVariables().isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"variables\": [] } }")));
    assertTrue(environment.getVariables().isEmpty());
    client.verify(list);
    client.clear(list);
  }


  @Test
  void listVariables_success(@Mock Environment mock) throws CloudManagerApiException {
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
  void setVariables_failure_400() {
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
    client.when(patch).respond(
        response()
            .withStatusCode(BAD_REQUEST_400.code())
            .withHeader("Content-Type", "application/problem+json")
            .withBody(json("{ \"type\" : \"http://ns.adobe.com/adobecloud/validation-exception\", \"errors\": [ \"some error\" ] }"))
    );
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).service(com.adobe.aio.cloudmanager.Environment.Tier.AUTHOR).build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).service(com.adobe.aio.cloudmanager.Environment.Tier.PUBLISH).build();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setVariables("1", "1", var1, var2), "Exception thrown.");
    assertEquals(String.format("Cannot set environment variables: %s/api/program/1/environment/1/variables (400 Bad Request) - Validation Error(s): some error.", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(patch);
    client.clear(patch);
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
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).service(com.adobe.aio.cloudmanager.Environment.Tier.AUTHOR).build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).service(com.adobe.aio.cloudmanager.Environment.Tier.PUBLISH).build();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setVariables("1", "1", var1, var2), "Exception thrown.");
    assertEquals(String.format("Cannot set environment variables: %s/api/program/1/environment/1/variables (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(patch);
    client.clear(patch);
  }

  // I have no idea how this case would be possible, but it was in the original impl, so i'm keeping it, and therefore need a test for it.
  @Test
  void setVariables_empty(@Mock Environment mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    com.adobe.aio.cloudmanager.Environment environment = new EnvironmentImpl(mock, underTest);
    HttpRequest patch = request().withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withHeader("Content-Type", "application/json")
        .withPath("/api/program/1/environment/1/variables")
        .withBody(json("[ { " +
            "\"name\": \"foo\", \"value\": \"bar\", \"type\": \"string\", \"service\": \"author\" }, " +
            "{ \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\", \"service\": \"publish\" " +
            "} ]"));
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).service(com.adobe.aio.cloudmanager.Environment.Tier.AUTHOR).build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).service(com.adobe.aio.cloudmanager.Environment.Tier.PUBLISH).build();

    client.when(patch).respond(response().withBody(json("{}")));
    assertTrue(environment.setVariables(var1, var2).isEmpty());
    client.verify(patch);
    client.clear(patch);

    client.when(patch).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(environment.setVariables(var1, var2).isEmpty());
    client.verify(patch);
    client.clear(patch);

    client.when(patch).respond(response().withBody(json("{ \"_embedded\": { \"variables\": [] } }")));
    assertTrue(environment.setVariables(var1, var2).isEmpty());
    client.verify(patch);
    client.clear(patch);
  }

  @Test
  void setVariables_success(@Mock Environment mock) throws CloudManagerApiException {
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
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).service(com.adobe.aio.cloudmanager.Environment.Tier.AUTHOR).build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).service(com.adobe.aio.cloudmanager.Environment.Tier.PUBLISH).build();

    Set<Variable> variables = new EnvironmentImpl(mock, underTest).setVariables(var1, var2);
    assertEquals(2, variables.size(), "Correct response");
    client.verify(patch);
    client.clear(patch);
  }

  @Test
  void downloadLogs_redirect_failure_404() {
    LocalDate date = LocalDate.now().withYear(2019).withMonth(9).withDayOfMonth(8);
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withBody(LIST_LOGS_BODY));

    HttpRequest getRedirect = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("date", date.toString());
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/author-aemerror-%s.txt\" }", baseUrl, date))));

    HttpRequest getFile = request().withMethod("GET").withPath(String.format("/logs/author-aemerror-%s.txt", date));
    client.when(getFile).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadLogs("1", "1", option, 1, new File(".")), "Exception thrown.");
    assertEquals(String.format("Cannot download %s/api/program/1/environment/1/logs/download?service=author&name=aemerror&date=2019-09-08 to ./environment-1-author-aemerror-2019-09-08.log.gz (Cause: java.io.FileNotFoundException).", baseUrl, baseUrl), exception.getMessage(), "Message was correct");

    client.verify(listLogs, VerificationTimes.exactly(1));
    client.verify(getRedirect, VerificationTimes.exactly(1));
    client.verify(getFile, VerificationTimes.exactly(1));
    client.clear(listLogs);
    client.clear(getRedirect);
    client.clear(getFile);
  }


  @Test
  void downloadLogs_empty(@Mock Environment environment) throws CloudManagerApiException, IOException {
    File outputDir = Files.createTempDirectory("log-output").toFile();
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(environment.getId()).thenReturn("1");
    when(environment.getProgramId()).thenReturn("1");

    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withBody(json("{}")));
    assertTrue(new EnvironmentImpl(environment, underTest).downloadLogs(option, 1, outputDir).isEmpty());
    client.verify(listLogs);
    client.clear(listLogs);

    client.when(listLogs).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(new EnvironmentImpl(environment, underTest).downloadLogs(option, 1, outputDir).isEmpty());
    client.verify(listLogs);
    client.clear(listLogs);

    client.when(listLogs).respond(response().withBody(json("{ \"_embedded\": { \"downloads\": [] } }")));
    assertTrue(new EnvironmentImpl(environment, underTest).downloadLogs(option, 1, outputDir).isEmpty());
    client.verify(listLogs);
    client.clear(listLogs);
  }

    @Test
  void downloadLogs_success(@Mock Environment environment) throws CloudManagerApiException, IOException {
    byte[] zipBytes = IOUtils.toByteArray(EnvironmentTest.class.getClassLoader().getResourceAsStream("file.log.gz"));
    LocalDate firstDate = LocalDate.now().withYear(2019).withMonth(9).withDayOfMonth(8);
    LocalDate secondDate = LocalDate.now().withYear(2019).withMonth(9).withDayOfMonth(7);
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(environment.getId()).thenReturn("1");
    when(environment.getProgramId()).thenReturn("1");

    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withBody(LIST_LOGS_BODY));

    HttpRequest getFirstRedirect = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("date", firstDate.toString());
    client.when(getFirstRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/author-aemerror-%s.txt\" }", baseUrl, firstDate))));
    HttpRequest getSecondRedirect = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("date", secondDate.toString());
    client.when(getSecondRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/author-aemerror-%s.txt\" }", baseUrl, secondDate))));

    HttpRequest download1 = request().withMethod("GET").withPath(String.format("/logs/author-aemerror-%s.txt", firstDate));
    client.when(download1).respond(response().withBody(zipBytes));

    HttpRequest download2 = request().withMethod("GET").withPath(String.format("/logs/author-aemerror-%s.txt", secondDate));
    client.when(download2).respond(response().withBody(zipBytes));

    File outputDir = Files.createTempDirectory("log-output").toFile();
    List<EnvironmentLog> logs = new ArrayList<>(new EnvironmentImpl(environment, underTest).downloadLogs(option, 1, outputDir));
    assertEquals(2, logs.size(), "Correct Object response");
    assertEquals(outputDir + "/environment-1-author-aemerror-2019-09-08.log.gz", logs.get(0).getDownloadPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir + "/environment-1-author-aemerror-2019-09-08.log.gz")) > 0, "File is not empty.");
    assertEquals(outputDir + "/environment-1-author-aemerror-2019-09-07.log.gz", logs.get(1).getDownloadPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir + "/environment-1-author-aemerror-2019-09-07.log.gz")) > 0, "File is not empty.");

    client.verify(listLogs, VerificationTimes.exactly(1));
    client.verify(getFirstRedirect, VerificationTimes.exactly(1));
    client.verify(getSecondRedirect, VerificationTimes.exactly(1));
    client.verify(download1, VerificationTimes.exactly(1));
    client.verify(download2, VerificationTimes.exactly(1));
    client.clear(listLogs);
    client.clear(getFirstRedirect);
    client.clear(getSecondRedirect);
    client.clear(download1);
    client.clear(download2);
  }

  @Test
  void getDeveloperConsoleUrl() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withBody(LIST_BODY));

    List<com.adobe.aio.cloudmanager.Environment> environments = new ArrayList<>(underTest.list("1"));
    assertNotNull(environments.get(0).getDeveloperConsoleUrl());

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> environments.get(1).getDeveloperConsoleUrl(), "Exception was thrown");
    assertEquals("Environment 2 [TestProgram_stage] does not appear to support Developer Console.", exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void get_via_predicate() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(LIST_BODY));
    Optional<com.adobe.aio.cloudmanager.Environment> env = underTest.get("1", (e) -> false);
    assertTrue(env.isEmpty());
    env = underTest.get("1", new com.adobe.aio.cloudmanager.Environment.NamePredicate("TestProgram_prod"));
    assertTrue(env.isPresent());
    client.verify(get);
    client.clear(get);
  }

}

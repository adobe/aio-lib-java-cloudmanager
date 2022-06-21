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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Environment;
import com.adobe.aio.cloudmanager.EnvironmentLog;
import com.adobe.aio.cloudmanager.impl.model.LogOptionRepresentation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class EnvironmentApiTest extends AbstractApiClientTest {

  private static JsonBody allJson;
  private static JsonBody devJson;

  @BeforeAll
  protected static void beforeAll() {
    allJson = loadBodyJson("environment/list-full.json");
    devJson = loadBodyJson("environment/list-dev.json");

  }

  @Test
  void list_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("1"), "Exception thrown.");
    assertEquals(String.format("Could not find environments: %s/api/program/1/environments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
  }

  @Test
  void list_type_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments").withQueryStringParameter("type", "dev");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("1", Environment.Type.DEV), "Exception thrown.");
    assertEquals(String.format("Could not find environments: %s/api/program/1/environments?type=dev (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
  }

  @Test
  void list_successEmpty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"environments\": [] } }")));
    
    Collection<Environment> environments = underTest.listEnvironments("1");
    assertTrue(environments.isEmpty(), "Empty body returns zero length list");
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withBody(allJson));
    Collection<Environment> environments = underTest.listEnvironments("1");
    assertEquals(4, environments.size(), "Correct environment length list");
    client.clear(list);
  }

  @Test
  void list_success_type() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments").withQueryStringParameter("type", "dev");
    client.when(list).respond(response().withBody(devJson));
    Collection<Environment> environments = underTest.listEnvironments("1", Environment.Type.DEV);
    assertEquals(2, environments.size(), "Correct environment length list");
    client.clear(list);
  }

  @Test
  void get_notfound() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withBody(allJson));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getEnvironment("1", new Environment.IdPredicate("99")));
    assertEquals("Could not find environment Id='99' for program 1.", exception.getMessage(), "Message was correct.");
    client.clear(list);
  }

  @Test
  void getEnvironment() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(list).respond(response().withBody(allJson));
    Environment environment = underTest.getEnvironment("1", new Environment.NamePredicate("TestProgram_prod"));
    assertEquals("1", environment.getId(), "Id was correct");
    client.clear(list);
  }

  @Test
  void delete_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1/environment/1");
    client.when(delete).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteEnvironment("1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot delete environment: %s/api/program/1/environment/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void delete_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1/environment/1");
    client.when(delete).respond(response().withStatusCode(CREATED_201.code()));
    Environment environment = underTest.getEnvironment("1", new Environment.IdPredicate("1"));
    underTest.deleteEnvironment(environment);
    
    client.verify(get, VerificationTimes.exactly(1));
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(delete);
  }

  @Test
  void delete_via_environment_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1/environment/1");
    client.when(delete).respond(response().withStatusCode(CREATED_201.code()));
    Environment environment = underTest.getEnvironment("1", new Environment.IdPredicate("1"));
    environment.delete();
    
    client.verify(get, VerificationTimes.exactly(1));
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(delete);
  }

  @Test
  void getDeveloperConsoleUrl_missing() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));
    
    Environment environment = underTest.getEnvironment("1", new Environment.IdPredicate("3"));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> environment.getDeveloperConsoleUrl(), "Exception thrown");
    assertEquals("Environment 3 does not appear to support Developer Console.", exception.getMessage(), "Exception message is correct");
    client.clear(get);
  }

  @Test
  void getDeveloperConsoleUrl_success() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));
    Environment environment = underTest.getEnvironment("1", new Environment.IdPredicate("1"));
    assertEquals("https://github.com/adobe/aio-cli-plugin-cloudmanager", environment.getDeveloperConsoleUrl(), "URL correctly read");
    client.clear(get);
  }

  @Test
  void downloadLogs_listLogs_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));

    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author").withQueryStringParameter("name", "notfound").withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withStatusCode(NOT_FOUND_404.code()));
    
    Environment environment = underTest.getEnvironment("1", new Environment.IdPredicate("1"));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadEnvironmentLogs(environment, new LogOptionImpl(new LogOptionRepresentation().service("author").name("notfound")), 1, new File(".")), "Exception thrown.");
    assertEquals(String.format("Cannot get logs: %s/api/program/1/environment/1/logs?service=author&name=notfound&days=1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.exactly(1));
    client.verify(listLogs, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(listLogs);
  }

  @Test
  void downloadLogs_download_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));

    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "invalidurl")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withBody(loadBodyJson("environment/list-logs-error.json")));

    HttpRequest getLog = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "invalidurl")
        .withQueryStringParameter("date", "2019-09-08");
    client.when(getLog).respond(response().withStatusCode(NOT_FOUND_404.code()));
    
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadEnvironmentLogs("1", "1", new LogOptionImpl(new LogOptionRepresentation().service("author").name("invalidurl")), 1, new File(".")), "Exception thrown.");
    assertEquals(String.format("Cannot get logs: %s/api/program/1/environment/1/logs/download?service=author&name=invalidurl&date=2019-09-08 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.clear(get);
    client.clear(listLogs);
  }

  @Test
  void downloadLogs_failure_invalidurl() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));

    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "invalidurl")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withBody(loadBodyJson("environment/list-logs-error.json")));

    HttpRequest redirect = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "invalidurl")
        .withQueryStringParameter("date", "2019-09-08");
    client.when(redirect).
        respond(response()
            .withContentType(MediaType.APPLICATION_JSON)
            .withBody(json("{ \"redirect\": \"git://logs/author_aemerror_2019-09-8.log.gz\" }")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadEnvironmentLogs("1", "1", new LogOptionImpl(new LogOptionRepresentation().service("author").name("invalidurl")), 1, new File(".")), "Exception thrown.");
    assertEquals("Log [/api/program/1/environment/1/logs/download?service=author&name=invalidurl&date=2019-09-08] did not contain a redirect. Was: unknown protocol: git.", exception.getMessage(), "Message was correct");

    client.clear(get);
    client.clear(listLogs);
    client.clear(redirect);
  }

  @Test
  void downloadLogs_noLogs() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));

    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withBody(loadBodyJson("environment/list-logs-emptylist.json")));

    Collection<EnvironmentLog> logs = underTest.downloadEnvironmentLogs("1", "1", new LogOptionImpl(new LogOptionRepresentation().service("author").name("aemerror")), 1, new File("."));
    assertTrue(logs.isEmpty(), "List was empty.");
    client.clear(get);
    client.clear(listLogs);
  }

  @Test
  void downloadLogs_success() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    byte[] zipBytes = IOUtils.toByteArray(EnvironmentApiTest.class.getClassLoader().getResourceAsStream("file.log.gz"));
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));

    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withBody( loadBodyJson("environment/list-logs-success.json")));

    HttpRequest redirect1 = request().withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("date", "2019-09-08");

    client.when(redirect1).respond(
        response()
            .withContentType(MediaType.APPLICATION_JSON)
            .withBody(String.format("{ \"redirect\": \"%s/logs/author_aemerror_2019-09-08.log.gz\" }", baseUrl))
    );

    HttpRequest redirect2 = request().withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("date", "2019-09-07");

    client.when(redirect2).respond(
        response()
            .withContentType(MediaType.APPLICATION_JSON)
            .withBody(String.format("{ \"redirect\": \"%s/logs/author_aemerror_2019-09-07.log.gz\" }", baseUrl))
    );

    HttpRequest download1 = request().withMethod("GET").withPath("/logs/author_aemerror_2019-09-08.log.gz");
    client.when(download1).respond(response().withBody(zipBytes));

    HttpRequest download2 = request().withMethod("GET").withPath("/logs/author_aemerror_2019-09-07.log.gz");
    client.when(download2).respond(response().withBody(zipBytes));

    File outputDir = Files.createTempDirectory("log-output").toFile();
    List<EnvironmentLog> logs = new ArrayList<>(underTest.downloadEnvironmentLogs("1", "1", new LogOptionImpl(new LogOptionRepresentation().service("author").name("aemerror")), 1, outputDir));
    assertEquals(2, logs.size(), "Correct Object response");
    assertEquals(outputDir + "/1-author-aemerror-2019-09-08.log.gz", logs.get(0).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir + "/1-author-aemerror-2019-09-08.log.gz")) > 0, "File is not empty.");
    assertEquals(outputDir + "/1-author-aemerror-2019-09-07.log.gz", logs.get(1).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir + "/1-author-aemerror-2019-09-07.log.gz")) > 0, "File is not empty.");

    client.clear(get);
    client.clear(listLogs);
    client.clear(redirect1);
    client.clear(redirect2);
    client.clear(download1);
    client.clear(download2);
  }

  @Test
  void downloadLogs_via_environment_success() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    byte[] zipBytes = IOUtils.toByteArray(EnvironmentApiTest.class.getClassLoader().getResourceAsStream("file.log.gz"));
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(response().withBody(allJson));

    JsonBody body = loadBodyJson("environment/list-logs-success.json");
    HttpRequest listLogs = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withBody(body));

    HttpRequest redirect1 = request().withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("date", "2019-09-08");

    client.when(redirect1).respond(
        response()
            .withContentType(MediaType.APPLICATION_JSON)
            .withBody(String.format("{ \"redirect\": \"%s/logs/author_via_2019-09-08.log.gz\" }", baseUrl))
    );

    HttpRequest redirect2 = request().withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/logs/download")
        .withQueryStringParameter("date", "2019-09-07");

    client.when(redirect2).respond(
        response()
            .withContentType(MediaType.APPLICATION_JSON)
            .withBody(String.format("{ \"redirect\": \"%s/logs/author_via_2019-09-07.log.gz\" }", baseUrl))
    );

    HttpRequest download1 = request().withMethod("GET").withPath("/logs/author_via_2019-09-08.log.gz");
    client.when(download1).respond(response().withBody(zipBytes));

    HttpRequest download2 = request().withMethod("GET").withPath("/logs/author_via_2019-09-07.log.gz");
    client.when(download2).respond(response().withBody(zipBytes));

    File outputDir = Files.createTempDirectory("log-output").toFile();

    Environment environment = underTest.getEnvironment("1", new Environment.IdPredicate("1"));
    List<EnvironmentLog> logs = new ArrayList<>(environment.downloadLogs(new LogOptionImpl(new LogOptionRepresentation().service("author").name("aemerror")), 1, outputDir));
    assertEquals(2, logs.size(), "Correct Object response");
    assertEquals(outputDir + "/1-author-aemerror-2019-09-08.log.gz", logs.get(0).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir + "/1-author-aemerror-2019-09-08.log.gz")) > 0, "File is not empty.");
    assertEquals(outputDir + "/1-author-aemerror-2019-09-07.log.gz", logs.get(1).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir + "/1-author-aemerror-2019-09-07.log.gz")) > 0, "File is not empty.");

    client.clear(get);
    client.clear(listLogs);
    client.clear(redirect1);
    client.clear(redirect2);
    client.clear(download1);
    client.clear(download2);
  }

}

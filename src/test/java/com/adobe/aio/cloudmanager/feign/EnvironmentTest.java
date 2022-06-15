package com.adobe.aio.cloudmanager.feign;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Environment;
import com.adobe.aio.cloudmanager.EnvironmentLog;
import com.adobe.aio.cloudmanager.generated.model.LogOptionRepresentation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.model.ClearType;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;

public class EnvironmentTest extends AbstractApiClientTest {

  private static String allJson;
  private static String devJson;
  
  @BeforeAll
  protected static void beforeAll() throws IOException {
    allJson = loadBodyJson("environments/environment-list-full.json");
    devJson = loadBodyJson("environments/environment-list-dev.json");
    
  }
  
  @Test
  void listEnvironments_failure404() {
    HttpRequest request = request().withMethod("GET").withPath("/api/program/1/environments");
    client.when(request).respond(
        response().withStatusCode(NOT_FOUND_404.code())
    );
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("1"), "Exception thrown for 404");
    assertEquals(String.format("Could not find environments: %s/api/program/1/environments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(request, ClearType.ALL);
  }

  @Test
  void listEnvironments_type_failure404() {
    HttpRequest request = request().withMethod("GET").withPath("/api/program/1/environments").withQueryStringParameter("type", "dev");
    client.when(request).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("1", Environment.Type.DEV), "Exception thrown for 404");
    assertEquals(String.format("Could not find environments: %s/api/program/1/environments?type=dev (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(request, ClearType.ALL);
  }

  @Test
  void listEnvironments_successEmpty() throws CloudManagerApiException {
    HttpRequest request = request().withMethod("GET").withPath("/api/program/1/environments");
    client.when(request).respond(
        response()
            .withStatusCode(OK_200.code())
            .withBody("{ \"_embedded\": { \"environments\": [] } }")
    );
    Collection<Environment> environments = underTest.listEnvironments("1");
    assertTrue(environments.isEmpty(), "Empty body returns zero length list");
    client.clear(request, ClearType.ALL);
  }

  @Test
  void listEnvironments_success() throws CloudManagerApiException {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));
    Collection<Environment> environments = underTest.listEnvironments("2");
    assertEquals(4, environments.size(), "Correct environment length list");
    client.clear(get, ClearType.ALL);
  }

  @Test
  void listEnvironments_success_type() throws CloudManagerApiException {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments").withQueryStringParameter("type", "dev");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(devJson));
    Collection<Environment> environments = underTest.listEnvironments("2", Environment.Type.DEV);
    assertEquals(2, environments.size(), "Correct environment length list");
    client.clear(get, ClearType.ALL);
  }

  @Test
  void getEnvironment_notfound404() {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getEnvironment("2", new Environment.IdPredicate("5")));
    assertEquals("Could not find environment Id='5' for program 2.", exception.getMessage(), "Message was correct.");
    client.clear(get, ClearType.ALL);
  }

  @Test
  void getEnvironment() throws CloudManagerApiException {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));
    Environment environment = underTest.getEnvironment("2", new Environment.NamePredicate("TestProgram_prod"));
    assertEquals("1", environment.getId(), "Id was correct");
    client.clear(get, ClearType.ALL);
  }
  
  @Test
  void deleteEnvironment_failure404() {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));
    HttpRequest delete = request().withMethod("DELETE").withPath("/api/program/2/environment/2");
    client.when(delete).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteEnvironment("2", "2"), "Exception thrown");
    assertEquals(String.format("Cannot delete environment: %s/api/program/2/environment/2 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct.");
    client.clear(get, ClearType.ALL);
    client.clear(delete, ClearType.ALL);
  }
  
  @Test
  void deleteEnvironment_success() throws CloudManagerApiException {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));
    HttpRequest delete = request().withMethod("DELETE").withPath("/api/program/2/environment/2");
    client.when(delete).respond(response().withStatusCode(CREATED_201.code()));
    Environment environment = underTest.getEnvironment("2", new Environment.IdPredicate("2"));
    underTest.deleteEnvironment(environment);
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(get, ClearType.ALL);
    client.clear(delete, ClearType.ALL);
  }

  @Test
  void deleteEnvironment_via_environment_success() throws CloudManagerApiException {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));
    HttpRequest delete = request().withMethod("DELETE").withPath("/api/program/2/environment/2");
    client.when(delete).respond(response().withStatusCode(CREATED_201.code()));
    Environment environment = underTest.getEnvironment("2", new Environment.IdPredicate("2"));
    environment.delete();
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(get, ClearType.ALL);
    client.clear(delete, ClearType.ALL);
  }
  
  @Test
  void getDeveloperConsoleUrl_missing() throws Exception {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));
    Collection<Environment> environments = underTest.listEnvironments("2");
    Environment environment = environments.stream().filter(e -> e.getId().equals("3")).findFirst().orElseThrow(Exception::new);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> environment.getDeveloperConsoleUrl(), "Exception thrown");
    assertEquals("Environment 3 does not appear to support Developer Console.", exception.getMessage(), "Exception message is correct");
    client.clear(get, ClearType.ALL);
  }
  
  @Test
  void getDeveloperConsoleUrl_success() throws Exception {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));
    Collection<Environment> environments = underTest.listEnvironments("2");
    Environment environment = environments.stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    String url = environment.getDeveloperConsoleUrl();
    assertEquals("https://github.com/adobe/aio-cli-plugin-cloudmanager", url, "URL correctly read");
    client.clear(get, ClearType.ALL);
  }

  @Test
  void downloadLogs_listLogs_failure404() throws CloudManagerApiException {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));

    Environment environment = underTest.getEnvironment("2", new Environment.IdPredicate("3"));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadEnvironmentLogs(environment, new LogOptionImpl(new LogOptionRepresentation().service("author").name("notfound")), 1, new File(".")), "Exception thrown for invalid url");
    assertEquals(String.format("Cannot get logs: %s/api/program/2/environment/3/logs?service=author&name=notfound&days=1 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");

    client.clear(get, ClearType.ALL);
  }

  @Test
  void downloadLogs_download_failure404() {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));

    String errors = loadBodyJson("environments/list-logs-error.json");
    HttpRequest listLogs = request()
        .withMethod("GET")
        .withPath("/api/program/2/environment/3/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "invalidurl")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withStatusCode(OK_200.code()).withBody(errors));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadEnvironmentLogs("2", "3", new LogOptionImpl(new LogOptionRepresentation().service("author").name("invalidurl")), 1, new File(".")), "Exception thrown for invalid url");
    assertEquals(String.format("Cannot get logs: %s/api/program/2/environment/3/logs/download?service=author&name=invalidurl&date=2019-09-08 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");

    client.clear(get, ClearType.ALL);
    client.clear(listLogs, ClearType.ALL);
  }

  @Test
  void downloadLogs_failure_invalidurl() {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));

    String body = loadBodyJson("environments/list-logs-error.json");
    HttpRequest listLogs = request()
        .withMethod("GET")
        .withPath("/api/program/2/environment/3/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "invalidurl")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withStatusCode(OK_200.code()).withBody(body));

    HttpRequest redirect = request()
        .withMethod("GET")
        .withPath("/api/program/2/environment/3/logs/download")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "invalidurl")
        .withQueryStringParameter("date", "2019-09-08");
    client.when(redirect).
        respond(response()
            .withStatusCode(OK_200.code())
            .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
            .withBody("{ \"redirect\": \"git://logs/author_aemerror_2019-09-8.log.gz\" }"));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadEnvironmentLogs("2", "3", new LogOptionImpl(new LogOptionRepresentation().service("author").name("invalidurl")), 1, new File(".")), "Exception thrown for invalid url");
    assertEquals(String.format("Log %s did not contain a redirect. Was: %s.", "/api/program/2/environment/3/logs/download?service=author&name=invalidurl&date=2019-09-08", "unknown protocol: git"), exception.getMessage(), "Message was correct");

    client.clear(get, ClearType.ALL);
    client.clear(listLogs, ClearType.ALL);
    client.clear(redirect, ClearType.ALL);
  }
  
  @Test
  void downloadLogs_noLogs() throws CloudManagerApiException {
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));

    String body = loadBodyJson("environments/list-logs-emptylist.json");
    HttpRequest listLogs = request()
        .withMethod("GET")
        .withPath("/api/program/2/environment/2/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withStatusCode(OK_200.code()).withBody(body));
    
    Collection<EnvironmentLog> logs = underTest.downloadEnvironmentLogs("2", "2", new LogOptionImpl(new LogOptionRepresentation().service("author").name("aemerror")), 1, new File("."));
    assertTrue(logs.isEmpty(), "List was empty.");
    client.clear(get, ClearType.ALL);
    client.clear(listLogs, ClearType.ALL);
  }
  
  @Test
  void downloadLogs_success() throws CloudManagerApiException, IOException {
    byte[] zipBytes = IOUtils.toByteArray(EnvironmentTest.class.getClassLoader().getResourceAsStream("file.log.gz"));
    HttpRequest get = request().withMethod("GET").withPath("/api/program/2/environments");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(allJson));

    String body = loadBodyJson("environments/list-logs-success.json");
    HttpRequest listLogs = request()
        .withMethod("GET")
        .withPath("/api/program/2/environment/1/logs")
        .withQueryStringParameter("service", "author")
        .withQueryStringParameter("name", "aemerror")
        .withQueryStringParameter("days", "1");
    client.when(listLogs).respond(response().withStatusCode(OK_200.code()).withBody(body));
    
    HttpRequest redirect1 = request().withMethod("GET")
        .withPath("/api/program/2/environment/1/logs/download")
        .withQueryStringParameter("date", "2019-09-08");
    
    client.when(redirect1).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
            .withBody(String.format("{ \"redirect\": \"%s/logs/author_aemerror_2019-09-08.log.gz\" }", baseUrl))
    );

    HttpRequest redirect2 = request().withMethod("GET")
        .withPath("/api/program/2/environment/1/logs/download")
        .withQueryStringParameter("date", "2019-09-07");

    client.when(redirect2).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
            .withBody(String.format("{ \"redirect\": \"%s/logs/author_aemerror_2019-09-07.log.gz\" }", baseUrl))
    );
    
    HttpRequest download1 = request().withMethod("GET").withPath("/logs/author_aemerror_2019-09-08.log.gz");
    client.when(download1).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(zipBytes));
    
    HttpRequest download2 = request().withMethod("GET").withPath("/logs/author_aemerror_2019-09-07.log.gz");
    client.when(download2).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(zipBytes));

    File outputDir = Files.createTempDirectory("log-output").toFile();
    List<EnvironmentLog> logs = new ArrayList<>(underTest.downloadEnvironmentLogs("2", "1", new LogOptionImpl(new LogOptionRepresentation().service("author").name("aemerror")), 1, outputDir));
    assertEquals(2, logs.size(), "Correct Object response");
    assertEquals(outputDir + "/1-author-aemerror-2019-09-08.log.gz", logs.get(0).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir + "/1-author-aemerror-2019-09-08.log.gz")) > 0, "File is not empty.");
    assertEquals(outputDir + "/1-author-aemerror-2019-09-07.log.gz", logs.get(1).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir + "/1-author-aemerror-2019-09-07.log.gz")) > 0, "File is not empty.");

    client.clear(get, ClearType.ALL);
    client.clear(listLogs, ClearType.ALL);
    client.clear(redirect1, ClearType.ALL);
    client.clear(redirect2, ClearType.ALL);
    client.clear(download1, ClearType.ALL);
    client.clear(download2, ClearType.ALL);
  }
}

package io.adobe.cloudmanager.impl;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 Adobe Inc.
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
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.model.Environment;
import io.adobe.cloudmanager.model.EnvironmentLog;
import io.adobe.cloudmanager.model.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.BinaryBody;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.*;

class EnvironmentsTest extends AbstractApiTest {

  private static byte[] zipBytes = null;

  public static List<String> getTestExpectationFiles() {
    return Arrays.asList(
        "environments/not-found.json",
        "environments/list-empty.json",
        "environments/list-success.json",
        "environments/delete-fails.json",
        "environments/delete-success.json",
        "environments/variables-not-found.json",
        "environments/variables-list-empty.json",
        "environments/variables-list-success.json",
        "environments/set-variables-bad-request.json",
        "environments/set-variables-list-empty.json",
        "environments/set-variables-variables-only.json",
        "environments/set-variables-secrets-only.json",
        "environments/set-variables-mixed.json",
        "environments/download-logs-not-found.json",
        "environments/download-logs-success.json"
    );
  }

  @BeforeEach
  public void setupLogsforEnvironments() throws IOException {
    if (zipBytes == null) {
      zipBytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("file.log.gz"));
      client.when(
          request().withMethod("GET")
              .withPath("/api/program/2/environment/1/logs/download")
              .withQueryStringParameter("date", "2019-09-8")
      ).respond(
          HttpResponse.response()
              .withStatusCode(HttpStatusCode.OK_200.code())
              .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .withBody(String.format("{ \"redirect\": \"%s/logs/author_aemerror_2019-09-8.log.gz\" }", baseUrl))
      );
      client.when(
          request().withMethod("GET")
              .withPath("/api/program/2/environment/1/logs/download")
              .withQueryStringParameter("date", "2019-09-7")
      ).respond(
          HttpResponse.response()
              .withStatusCode(HttpStatusCode.OK_200.code())
              .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
              .withBody(String.format("{ \"redirect\": \"%s/logs/author_aemerror_2019-09-7.log.gz\" }", baseUrl))
      );

      client.when(
          request().withMethod("GET").withPath("/logs/author_aemerror_2019-09-8.log.gz")
      ).respond(
          HttpResponse.response()
              .withStatusCode(HttpStatusCode.OK_200.code())
              .withBody(BinaryBody.binary(zipBytes))
      );
      client.when(
          request().withMethod("GET").withPath("/logs/author_aemerror_2019-09-7.log.gz")
      ).respond(
          HttpResponse.response()
            .withStatusCode(HttpStatusCode.OK_200.code())
            .withBody(BinaryBody.binary(zipBytes))
      );
    }
  }

  @Test
  void listEnvironments_failure() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("1"), "Exception thrown for 404");
    assertEquals(String.format("Could not find environments: %s/api/program/1/environments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listEnvironments_successEmpty() throws CloudManagerApiException {
    List<Environment> environments = underTest.listEnvironments("3");
    assertTrue(environments.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void listEnvironments_success() throws CloudManagerApiException {
    List<Environment> environments = underTest.listEnvironments("2");
    assertEquals(4, environments.size(), "Correct environment length list");
  }

  @Test
  void listEnvironments_badProgram() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("8"), "Exception thrown for 404");
    assertEquals("Could not find program 8", exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_deleteReturns400() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteEnvironment("2", "3"), "Exception thrown for 404");
    assertEquals(String.format("Cannot delete environment: %s/api/program/2/environment/3 (400 Bad Request)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_badEnvironment() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteEnvironment("2", "12"), "Exception thrown for 404");
    assertEquals("Could not find environment 12 for program 2.", exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_success() throws CloudManagerApiException {
    underTest.deleteEnvironment("2", "1");

    client.verify(request().withMethod("DELETE").withPath("/api/program/2/environment/1"));
  }

  @Test
  void getDeveloperConsoleUrl_missing() throws Exception {
    List<Environment> environments = underTest.listEnvironments("2");
    Environment environment = environments.stream().filter(e -> e.getId().equals("3")).findFirst().orElseThrow(Exception::new);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> environment.getDeveloperConsoleUrl(), "Exception thrown");
    assertEquals("Environment 3 does not appear to support Developer Console.", exception.getMessage(), "Exception message is correct");
  }

  @Test
  void getDeveloperConsoleUrl_success() throws Exception {
    List<Environment> environments = underTest.listEnvironments("2");
    Environment environment = environments.stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    String url = environment.getDeveloperConsoleUrl();
    assertEquals("https://github.com/adobe/aio-cli-plugin-cloudmanager", url, "URL correctly read");
  }

  @Test
  void getEnvironmentVariables_environmentNotFound() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getEnvironmentVariables("1", "1"), "Exception thrown for 404");
    assertEquals(String.format("Could not find environments: %s/api/program/1/environments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getEnvironmentVariables_noEnvironment() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getEnvironmentVariables("2", "12"), "Exception thrown for 404");
    assertEquals("Could not find environment 12 for program 2.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getEnvironmentVariables_noLink() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getEnvironmentVariables("2", "2"), "Exception thrown for missing link");
    assertEquals("Could not find variables link for environment 2 for program 2.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getEnvironmentVariables_linkReturnsNotFound() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getEnvironmentVariables("2", "3"), "Exception thrown for 404");
    assertEquals(String.format("Cannot get variables: %s/api/program/2/environment/3/variables (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getEnvironmentVariables_emptyList() throws CloudManagerApiException {
    List<Variable> variables = underTest.getEnvironmentVariables("2", "4");
    assertTrue(variables.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void getEnvironmentVariables_success() throws CloudManagerApiException {
    List<Variable> variables = underTest.getEnvironmentVariables("2", "1");
    assertEquals(2, variables.size(), "Empty body returns zero length list");
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));
  }

  @Test
  void getEnvironmentVariables_successEnvironment() throws Exception {
    Environment environment = underTest.listEnvironments("2").stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    List<Variable> variables = underTest.getEnvironmentVariables(environment);
    assertEquals(2, variables.size(), "Empty body returns zero length list");
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));
  }

  @Test
  void getEnvironmentVariables_via_environment() throws Exception {
    Environment environment = underTest.listEnvironments("2").stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    List<Variable> variables = environment.getVariables();
    assertEquals(2, variables.size(), "Empty body returns zero length list");
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));
  }

  @Test
  void setEnvironmentVariable_environmentNotFound() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setEnvironmentVariables("1", "1"), "Exception thrown for 404");
    assertEquals(String.format("Could not find environments: %s/api/program/1/environments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void setEnvironmentVariables_noEnvironment() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setEnvironmentVariables("2", "12"), "Exception thrown for 404");
    assertEquals("Could not find environment 12 for program 2.", exception.getMessage(), "Message was correct");
  }

  @Test
  void setEnvironmentVariables_noLink() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setEnvironmentVariables("2", "2"), "Exception thrown for missing link");
    assertEquals("Could not find variables link for environment 2 for program 2.", exception.getMessage(), "Message was correct");
  }

  @Test
  void setEnvironmentVariables_patchFails() {
    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setEnvironmentVariables("2", "3", v), "Exception thrown for failure");
    assertEquals(String.format("Cannot set variables: %s/api/program/2/environment/3/variables (400 Bad Request) - Validation Error(s): some error", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void setEnvironmentVariables_successEmpty() throws CloudManagerApiException {
    List<Variable> results = underTest.setEnvironmentVariables("2", "4");
    assertTrue(results.isEmpty());
    client.verify(request().withMethod("PATCH").withPath("/api/program/2/environment/4/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void setEnvironmentVariables_variablesOnly() throws CloudManagerApiException {
    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("foo2");
    v2.setValue("bar2");

    List<Variable> results = underTest.setEnvironmentVariables("2", "1", v, v2);
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.STRING);
    v2.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.STRING);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(request().withMethod("PATCH").withPath("/api/program/2/environment/1/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void setEnvironmentVariables_secretsOnly() throws CloudManagerApiException {
    Variable v = new Variable();
    v.setName("secretFoo");
    v.setValue("secretBar");
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.SECRETSTRING);

    Variable v2 = new Variable();
    v2.setName("secretFoo2");
    v2.setValue("secretBar2");
    v2.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.SECRETSTRING);

    List<Variable> results = underTest.setEnvironmentVariables("2", "1", v, v2);
    v.setValue(null);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains secretFoo");
    assertTrue(results.contains(v2), "Results contains secretFoo2");
    client.verify(request().withMethod("PATCH").withPath("/api/program/2/environment/1/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void setEnvironmentVariables_mixed() throws CloudManagerApiException {
    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("secretFoo");
    v2.setValue("secretBar");
    v2.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.SECRETSTRING);

    List<Variable> results = underTest.setEnvironmentVariables("2", "1", v, v2);
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.STRING);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains secretFoo");
    client.verify(request().withMethod("PATCH").withPath("/api/program/2/environment/1/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void setEnvironmentVariables_via_environment() throws Exception {
    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("secretFoo");
    v2.setValue("secretBar");
    v2.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.SECRETSTRING);

    Environment env = underTest.listEnvironments("2").stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);

    List<Variable> results = env.setVariables(v, v2);
    v.setType(io.adobe.cloudmanager.swagger.model.Variable.TypeEnum.STRING);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains secretFoo");
    client.verify(request().withMethod("PATCH").withPath("/api/program/2/environment/1/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void downloadLogs_fails() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadLogs("2", "12", "service", "name", 1, new File(".")), "Exception thrown for 404");
    assertEquals("Could not find environment 12 for program 2.", exception.getMessage(), "Message was correct");
  }

  @Test
  void downloadLogs_noLogLink() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadLogs("2", "2", "service", "name", 1, new File(".")), "Exception thrown for missing link");
    assertEquals("Could not find logs link for environment 2 for program 2.", exception.getMessage(), "Message was correct");
  }

  @Test
  void downloadLogs_getLogsFails() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.downloadLogs("2", "3", "author", "aemerror", 1, new File(".")), "Exception thrown for 404");
    assertEquals(String.format("Cannot get logs: %s/api/program/2/environment/3/logs?service=author&name=aemerror&days=1 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct.");
  }

  @Test
  void downloadLogs_success() throws CloudManagerApiException, IOException {
    File outputDir = Files.createTempDirectory("log-output").toFile();
    List<EnvironmentLog> logs = underTest.downloadLogs("2", "1", "author", "aemerror", 1, outputDir);
    assertEquals(2, logs.size(), "Correct Object response");
    assertEquals(outputDir.toString() + "/1-author-aemerror-2019-09-08.log.gz", logs.get(0).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir.toString() + "/1-author-aemerror-2019-09-08.log.gz")) > 0, "File is not empty.");
    assertEquals(outputDir.toString() + "/1-author-aemerror-2019-09-07.log.gz", logs.get(1).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir.toString() + "/1-author-aemerror-2019-09-07.log.gz")) > 0, "File is not empty.");
  }

  @Test
  void downloadLogs_successEnvironment() throws Exception {
    Environment env = underTest.listEnvironments("2").stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);

    File outputDir = Files.createTempDirectory("log-output").toFile();
    List<EnvironmentLog> logs = underTest.downloadLogs(env, "author", "aemerror", 1, outputDir);
    assertEquals(2, logs.size(), "Correct Object response");
    assertEquals(outputDir.toString() + "/1-author-aemerror-2019-09-08.log.gz", logs.get(0).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir.toString() + "/1-author-aemerror-2019-09-08.log.gz")) > 0, "File is not empty.");
    assertEquals(outputDir.toString() + "/1-author-aemerror-2019-09-07.log.gz", logs.get(1).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir.toString() + "/1-author-aemerror-2019-09-07.log.gz")) > 0, "File is not empty.");
  }

  @Test
  void downloadLogs_via_environment() throws Exception {
    Environment env = underTest.listEnvironments("2").stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);

    File outputDir = Files.createTempDirectory("log-output").toFile();
    List<EnvironmentLog> logs = env.downloadLogs("author", "aemerror", 1, outputDir);
    assertEquals(2, logs.size(), "Correct Object response");
    assertEquals(outputDir.toString() + "/1-author-aemerror-2019-09-08.log.gz", logs.get(0).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir.toString() + "/1-author-aemerror-2019-09-08.log.gz")) > 0, "File is not empty.");
    assertEquals(outputDir.toString() + "/1-author-aemerror-2019-09-07.log.gz", logs.get(1).getPath(), "Log file exists.");
    assertTrue(FileUtils.sizeOf(new File(outputDir.toString() + "/1-author-aemerror-2019-09-07.log.gz")) > 0, "File is not empty.");
  }

}

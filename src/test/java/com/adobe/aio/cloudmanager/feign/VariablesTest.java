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

import java.util.Set;
import java.util.UUID;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Environment;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.Variable;
import org.junit.jupiter.api.Test;
import org.mockserver.model.ClearType;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class VariablesTest extends AbstractApiClientTest {

  @Test
  void listEnvironmentVariables_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/program/1/environment/1/variables");
    client.when(list).respond(HttpResponse.response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironmentVariables("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get variables: %s/api/program/1/environment/1/variables (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(list, ClearType.ALL);
  }

  @Test
  void listEnvironmentVariables_emptyList() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environment/1/variables");
    client.when(list).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("variables/list-environment-empty.json")));
    Set<Variable> variables = underTest.listEnvironmentVariables("1", "1");
    assertTrue(variables.isEmpty(), "Empty body returns zero length list");
    client.clear(list, ClearType.ALL);
  }

  @Test
  void listEnvironmentVariables_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environment/1/variables");
    client.when(list).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("variables/list-environment-success.json")));
    Set<Variable> variables = underTest.listEnvironmentVariables("1", "1");
    assertEquals(2, variables.size(), "Empty body returns zero length list");
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));
    client.clear(list, ClearType.ALL);
  }

  @Test
  void listEnvironmentVariables_via_environment() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("environments/list-full.json")));

    HttpRequest list = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environment/1/variables");
    client.when(list).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("variables/list-environment-success.json")));
    Environment environment = underTest.getEnvironment("1", new Environment.IdPredicate("1"));

    Set<Variable> variables = environment.listVariables();
    assertEquals(2, variables.size(), "Empty body returns zero length list");
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));

    client.clear(get, ClearType.ALL);
    client.clear(list, ClearType.ALL);
  }

  @Test
  void setEnvironmentVariables_failure400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/variables");

    client.when(set).respond(HttpResponse.response()
        .withStatusCode(BAD_REQUEST_400.code())
        .withHeader("Content-Type", "application/problem+json")
        .withBody(json("{ \"type\" : \"http://ns.adobe.com/adobecloud/validation-exception\", \"errors\": [ \"some error\" ] }"))
    );

    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setEnvironmentVariables("1", "1", v), "Exception thrown.");
    assertEquals(String.format("Cannot set variables: %s/api/program/1/environment/1/variables (400 Bad Request) - Validation Error(s): some error.", baseUrl), exception.getMessage(), "Message was correct");

    client.clear(set, ClearType.ALL);
  }

  @Test
  void setEnvironmentVariables_successEmpty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/variables")
        .withBody("[]");
    client.when(set).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("variables/set-environment-empty.json")));
    Set<Variable> variables = underTest.setEnvironmentVariables("1", "1");
    assertTrue(variables.isEmpty(), "Empty list returned");
    client.verify(set, VerificationTimes.exactly(1));
    client.clear(set, ClearType.ALL);
  }

  @Test
  void setEnvironmentVariables_variablesOnly() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\" }, { \"name\": \"foo2\", \"value\": \"bar2\" } ]"));

    client.when(set).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withBody(loadBodyJson("variables/set-environment-variables.json")));

    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("foo2");
    v2.setValue("bar2");

    Set<Variable> results = underTest.setEnvironmentVariables("1", "1", v, v2);
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v2.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(set.withContentType(MediaType.APPLICATION_JSON));
    client.clear(set, ClearType.ALL);
  }

  @Test
  void setEnvironmentVariables_secretsOnly() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/variables")
        .withBody(json("[ { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" }, { \"name\": \"secretFoo2\", \"value\": \"secretBar2\", \"type\": \"secretString\" } ]"));

    client.when(set).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withBody(loadBodyJson("variables/set-environment-secrets.json")));

    Variable v = new Variable();
    v.setName("secretFoo");
    v.setValue("secretBar");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);

    Variable v2 = new Variable();
    v2.setName("secretFoo2");
    v2.setValue("secretBar2");
    v2.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);

    Set<Variable> results = underTest.setEnvironmentVariables("1", "1", v, v2);
    v.setValue(null);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(set.withContentType(MediaType.APPLICATION_JSON));
    client.clear(set, ClearType.ALL);
  }

  @Test
  void setEnvironmentVariables_mixed() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\" }, { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" } ]"));

    client.when(set).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withBody(loadBodyJson("variables/set-environment-mixed.json")));

    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("secretFoo");
    v2.setValue("secretBar");
    v2.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);

    Set<Variable> results = underTest.setEnvironmentVariables("1", "1", v, v2);
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(set.withContentType(MediaType.APPLICATION_JSON));
    client.clear(set, ClearType.ALL);
  }

  @Test
  void setEnvironmentVariables_via_environment() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/environments");
    client.when(get).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("environments/list-full.json")));

    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/environment/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\" }, { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" } ]"));

    client.when(set).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withBody(loadBodyJson("variables/set-environment-mixed.json")));

    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("secretFoo");
    v2.setValue("secretBar");
    v2.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);

    Environment environment = underTest.getEnvironment("1", new Environment.IdPredicate("1"));
    Set<Variable> results = environment.setVariables(v, v2);
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(set.withContentType(MediaType.APPLICATION_JSON));
    client.clear(get, ClearType.ALL);
    client.clear(set, ClearType.ALL);
  }

  @Test
  void listPipelineVariables_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/variables");
    client.when(list).respond(HttpResponse.response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelineVariables("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get variables: %s/api/program/1/pipeline/1/variables (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(list, ClearType.ALL);
  }

  @Test
  void listPipelineVariables_emptyList() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/variables");
    client.when(list).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("variables/list-pipeline-empty.json")));
    Set<Variable> variables = underTest.listPipelineVariables("1", "1");
    assertTrue(variables.isEmpty(), "Empty body returns zero length list");
    client.clear(list, ClearType.ALL);
  }

  @Test
  void listPipelineVariables_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/variables");
    client.when(list).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("variables/list-pipeline-success.json")));
    Set<Variable> variables = underTest.listPipelineVariables("1", "1");
    assertEquals(2, variables.size(), "Empty body returns zero length list");
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));
    client.clear(list, ClearType.ALL);
  }

  @Test
  void listPipelineVariables_via_pipeline() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(get).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("pipelines/list-full.json")));

    HttpRequest list = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/variables");
    client.when(list).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("variables/list-pipeline-success.json")));
    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();

    Set<Variable> variables = pipeline.listVariables();
    assertEquals(2, variables.size(), "Empty body returns zero length list");
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));

    client.clear(get, ClearType.ALL);
    client.clear(list, ClearType.ALL);
  }

  @Test
  void setPipelineVariables_failure400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/variables");

    client.when(set).respond(HttpResponse.response()
        .withStatusCode(BAD_REQUEST_400.code())
        .withHeader("Content-Type", "application/problem+json")
        .withBody(json("{ \"type\" : \"http://ns.adobe.com/adobecloud/validation-exception\", \"errors\": [ \"some error\" ] }"))
    );

    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setPipelineVariables("1", "1", v), "Exception thrown.");
    assertEquals(String.format("Cannot set variables: %s/api/program/1/pipeline/1/variables (400 Bad Request) - Validation Error(s): some error.", baseUrl), exception.getMessage(), "Message was correct");

    client.clear(set, ClearType.ALL);
  }

  @Test
  void setPipelineVariables_successEmpty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody("[]");
    client.when(set).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("variables/set-pipeline-empty.json")));
    Set<Variable> variables = underTest.setPipelineVariables("1", "1");
    assertTrue(variables.isEmpty(), "Empty list returned");
    client.verify(set, VerificationTimes.exactly(1));
    client.clear(set, ClearType.ALL);
  }

  @Test
  void setPipelineVariables_variablesOnly() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\" }, { \"name\": \"foo2\", \"value\": \"bar2\" } ]"));

    client.when(set).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withBody(loadBodyJson("variables/set-pipeline-variables.json")));

    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("foo2");
    v2.setValue("bar2");

    Set<Variable> results = underTest.setPipelineVariables("1", "1", v, v2);
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v2.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(set.withContentType(MediaType.APPLICATION_JSON));
    client.clear(set, ClearType.ALL);
  }

  @Test
  void setPipelineVariables_secretsOnly() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody(json("[ { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" }, { \"name\": \"secretFoo2\", \"value\": \"secretBar2\", \"type\": \"secretString\" } ]"));

    client.when(set).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withBody(loadBodyJson("variables/set-pipeline-secrets.json")));

    Variable v = new Variable();
    v.setName("secretFoo");
    v.setValue("secretBar");
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);

    Variable v2 = new Variable();
    v2.setName("secretFoo2");
    v2.setValue("secretBar2");
    v2.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);

    Set<Variable> results = underTest.setPipelineVariables("1", "1", v, v2);
    v.setValue(null);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(set.withContentType(MediaType.APPLICATION_JSON));
    client.clear(set, ClearType.ALL);
  }

  @Test
  void setPipelineVariables_mixed() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\" }, { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" } ]"));

    client.when(set).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withBody(loadBodyJson("variables/set-pipeline-mixed.json")));

    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("secretFoo");
    v2.setValue("secretBar");
    v2.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);

    Set<Variable> results = underTest.setPipelineVariables("1", "1", v, v2);
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(set.withContentType(MediaType.APPLICATION_JSON));
    client.clear(set, ClearType.ALL);
  }

  @Test
  void setPipelineVariables_via_pipeline() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = HttpRequest.request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(get).respond(HttpResponse.response().withStatusCode(OK_200.code()).withBody(loadBodyJson("pipelines/list-full.json")));

    HttpRequest set = HttpRequest.request()
        .withMethod("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\" }, { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" } ]"));

    client.when(set).respond(
        HttpResponse.response()
            .withStatusCode(OK_200.code())
            .withBody(loadBodyJson("variables/set-pipeline-mixed.json")));

    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("secretFoo");
    v2.setValue("secretBar");
    v2.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.SECRETSTRING);

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    Set<Variable> results = pipeline.setVariables(v, v2);
    v.setType(com.adobe.aio.cloudmanager.generated.model.Variable.TypeEnum.STRING);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(set.withContentType(MediaType.APPLICATION_JSON));
    client.clear(get, ClearType.ALL);
    client.clear(set, ClearType.ALL);
  }

}

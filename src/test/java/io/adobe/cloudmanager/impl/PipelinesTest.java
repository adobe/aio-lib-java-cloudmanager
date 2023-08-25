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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineUpdate;
import io.adobe.cloudmanager.impl.generated.PipelinePhase;
import io.adobe.cloudmanager.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;

class PipelinesTest extends AbstractApiTest {

  private String pipeline1Branch = "yellow";
  private String pipeline1RepositoryId = "1";

  public static List<String> getTestExpectationFiles() {
    return Arrays.asList(
        "pipelines/list-empty.json",
        "pipelines/list-success.json",
        "pipelines/start-execution-success.json",
        "pipelines/start-execution-fails-running.json",
        "pipelines/update-not-allowed.json",
        "pipelines/delete-bad-request.json",
        "pipelines/delete-success.json",
        "pipelines/variables-not-found.json",
        "pipelines/variables-list-empty.json",
        "pipelines/variables-list-success.json",
        "pipelines/set-variables-bad-request.json",
        "pipelines/set-variables-list-empty.json",
        "pipelines/set-variables-variables-only.json",
        "pipelines/set-variables-secrets-only.json",
        "pipelines/set-variables-mixed.json"
    );
  }

  @BeforeEach
  public void setupPipelinesForProgram3() {
    client.when(
        request().withMethod("PATCH").withPath("/api/program/3/pipeline/1").withContentType(MediaType.APPLICATION_JSON)
    ).respond(this::handleGoodPatch);
  }

  private HttpResponse handleGoodPatch(HttpRequest request) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    io.adobe.cloudmanager.impl.generated.Pipeline pipeline = objectMapper.readValue(request.getBodyAsJsonOrXmlString(), io.adobe.cloudmanager.impl.generated.Pipeline.class);

    Optional<PipelinePhase> buildPhase = pipeline.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType()).findFirst();
    if (buildPhase.isPresent()) {
      if (buildPhase.get().getBranch() != null) {
        pipeline1Branch = buildPhase.get().getBranch();
      }
      if (buildPhase.get().getRepositoryId() != null) {
        pipeline1RepositoryId = buildPhase.get().getRepositoryId();
      }
    }
    return response().withBody(buildPipeline1(), MediaType.APPLICATION_JSON);
  }

  @Test
  void listPipelines_successEmptyBody() throws CloudManagerApiException {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("6"), "Exception thrown for no body");
    assertEquals(String.format("Could not find pipelines for program %s", "6"), exception.getMessage(), "Message was correct");

  }

  @Test
  void listPipelines_successEmptyEmbedded() throws CloudManagerApiException {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("2"), "Exception thrown for no embedded");
    assertEquals(String.format("Could not find pipelines for program %s", "2"), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPipelines_successEmptyPipelines() throws CloudManagerApiException {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("7"), "Exception thrown for no pipelines");
    assertEquals(String.format("Could not find pipelines for program %s", "7"), exception.getMessage(), "Message was correct");
  }


  @Test
  void listPipelines_success() throws CloudManagerApiException {
    Collection<Pipeline> pipelines = underTest.listPipelines("3");
    assertEquals(4, pipelines.size(), "Correct pipelines list length");
  }

  @Test
  void listPipelines_programReturns404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("5"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve program: %s/api/program/5 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPipelines_programNotFound() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("8"), "Exception thrown for 404");
    assertEquals("Could not find program 8", exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_via_pipeline() throws Exception {
    Collection<Pipeline> pipelines = underTest.listPipelines("3");
    Pipeline pipeline = pipelines.stream().filter(p -> p.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    PipelineExecution execution = pipeline.startExecution();
    assertEquals("/api/program/3/pipeline/1/execution/5000", ((PipelineExecutionImpl) execution).getLinks().getSelf().getHref(), "URL was correct");
  }

  @Test
  void startExecution_badPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("3", "10"), "Exception thrown");
    assertEquals("Cannot start execution. Pipeline 10 does not exist in program 3.", exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_failsRunning() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("3", "2"), "Exception thrown");
    assertEquals("Cannot create execution. Pipeline already running.", exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_failsNotFound() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("3", "3"), "Exception thrown");
    assertEquals(String.format("Cannot create execution: %s/api/program/3/pipeline/3/execution (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.startExecution("3", "1");
    assertEquals("/api/program/3/pipeline/1/execution/5000", ((PipelineExecutionImpl) execution).getLinks().getSelf().getHref(), "URL was correct");
  }

  @Test
  void updatePipeline_badPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.updatePipeline("3", "10", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals("Pipeline 10 does not exist in program 3.", exception.getMessage(), "Message was correct");
  }

  @Test
  void updatePipeline_failure() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.updatePipeline("3", "4", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals(String.format("Cannot update pipeline: %s/api/program/3/pipeline/4 (405 Method Not Allowed)", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/4").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void updatePipeline_branchSuccess() throws CloudManagerApiException {
    Pipeline result = underTest.updatePipeline("3", "1", PipelineUpdate.builder().branch("develop").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("1");

    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/1").withContentType(MediaType.APPLICATION_JSON));

    assertThat("update was successful", ((PipelineImpl) result).getPhases(), hasItem(expected));
  }

  @Test
  void updatePipeline_repositoryAndBranchSuccess() throws CloudManagerApiException {
    Pipeline result = underTest.updatePipeline("3", "1", PipelineUpdate.builder().branch("develop").repositoryId("4").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("4");

    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/1").withContentType(MediaType.APPLICATION_JSON));

    assertThat("update was successful", ((PipelineImpl) result).getPhases(), hasItem(expected));
  }

  @Test
  void updatePipeline_via_pipeline() throws Exception {
    Collection<Pipeline> pipelines = underTest.listPipelines("3");
    Pipeline pipeline = pipelines.stream().filter(p -> p.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    Pipeline result = pipeline.update(PipelineUpdate.builder().branch("develop").repositoryId("4").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("4");

    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/1").withContentType(MediaType.APPLICATION_JSON));

    assertThat("update was successful", ((PipelineImpl) result).getPhases(), hasItem(expected));
  }

  @Test
  void deletePipeline_returns400() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deletePipeline("3", "4"), "Exception was thrown");
    assertEquals(String.format("Cannot delete pipeline: %s/api/program/3/pipeline/4 (400 Bad Request)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void deletePipeline_badPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.deletePipeline("3", "10"));
    assertEquals("Pipeline 10 does not exist in program 3.", exception.getMessage(), "Message was correct");
  }

  @Test
  void deletePipeline_success() throws CloudManagerApiException {
    underTest.deletePipeline("3", "1");

    client.verify(request().withMethod("DELETE").withPath("/api/program/3/pipeline/1"));
  }

  @Test
  void deletePipeline_viaPipeline() throws Exception {
    Collection<Pipeline> pipelines = underTest.listPipelines("3");
    Pipeline pipeline = pipelines.stream().filter(p -> p.getId().equals("1")).findFirst().orElseThrow(Exception::new);

    pipeline.delete();

    client.verify(request().withMethod("DELETE").withPath("/api/program/3/pipeline/1"));
  }

  @Test
  void getPipelineVariables_pipeline404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelineVariables("1", "1"), "Exception was thrown");
    assertEquals(String.format("Cannot retrieve pipelines: %s/api/program/1/pipelines (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getPipelineVariables_pipelineMissing() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelineVariables("3", "10"), "Exception was thrown");
    assertEquals("Pipeline 10 does not exist in program 3.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getPipelineVariables_noLink() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelineVariables("3", "2"), "Exception was thrown");
    assertEquals("Could not find variables link for pipeline 2 for program 3.", exception.getMessage(), "Message was correct.");
  }

  @Test
  void getPipelineVariables_link404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelineVariables("3", "3"), "Exception was thrown");
    assertEquals(String.format("Cannot get variables: %s/api/program/3/pipeline/3/variables (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct.");
  }

  @Test
  void getPipelineVariables_emptyList() throws CloudManagerApiException {
    Set<Variable> variables = underTest.listPipelineVariables("3", "4");
    assertTrue(variables.isEmpty(), "empty body return zero length list.");
  }

  @Test
  void getPipelineVariables_success() throws CloudManagerApiException {
    Set<Variable> variables = underTest.listPipelineVariables("3", "1");
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));
  }

  @Test
  void getPipelineVariables_successPipeline() throws CloudManagerApiException {
    Pipeline pipeline = underTest.listPipelines("3", p -> p.getId().equals("1")).stream().findFirst().orElse(null);
    Set<Variable> variables = underTest.listPipelineVariables(pipeline);
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));
  }

  @Test
  void getPipelineVariables_via_pipeline() throws CloudManagerApiException {
    Pipeline pipeline = underTest.listPipelines("3", p -> p.getId().equals("1")).stream().findFirst().orElse(null);
    Set<Variable> variables = pipeline.listVariables();
    Variable v = new Variable();
    v.setName("KEY");
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.STRING);
    v.setValue("value");
    assertTrue(variables.contains(v));
    v = new Variable();
    v.setName("I_AM_A_SECRET");
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.SECRETSTRING);
    assertTrue(variables.contains(v));
  }

  @Test
  void setPipelineVariables_pipeline404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setPipelineVariables("1", "1"), "Exception was thrown");
    assertEquals(String.format("Cannot retrieve pipelines: %s/api/program/1/pipelines (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void setPipelineVariables_pipelineMissing() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setPipelineVariables("3", "10"), "Exception was thrown");
    assertEquals("Pipeline 10 does not exist in program 3.", exception.getMessage(), "Message was correct");
  }

  @Test
  void setPipelineVariables_noLink() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setPipelineVariables("3", "2"), "Exception was thrown");
    assertEquals("Could not find variables link for pipeline 2 for program 3.", exception.getMessage(), "Message was correct.");
  }

  @Test
  void setPipelineVariables_patchFails() {
    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setPipelineVariables("3", "3", v), "Exception thrown for failure");
    assertEquals(String.format("Cannot set variables: %s/api/program/3/pipeline/3/variables (400 Bad Request) - Validation Error(s): some error", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void setPipelineVariables_successEmpty() throws CloudManagerApiException {
    Set<Variable> results = underTest.setPipelineVariables("3", "4");
    assertTrue(results.isEmpty());
    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/4/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void setPipelineVariables_variablesOnly() throws CloudManagerApiException {
    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("foo2");
    v2.setValue("bar2");

    Set<Variable> results = underTest.setPipelineVariables("3", "1", v, v2);
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.STRING);
    v2.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.STRING);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains foo2");
    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/1/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void setPipelineVariables_secretsOnly() throws CloudManagerApiException {
    Variable v = new Variable();
    v.setName("secretFoo");
    v.setValue("secretBar");
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.SECRETSTRING);

    Variable v2 = new Variable();
    v2.setName("secretFoo2");
    v2.setValue("secretBar2");
    v2.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.SECRETSTRING);

    Set<Variable> results = underTest.setPipelineVariables("3", "1", v, v2);
    v.setValue(null);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains secretFoo");
    assertTrue(results.contains(v2), "Results contains secretFoo2");
    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/1/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void setPipelineVariables_mixed() throws CloudManagerApiException {
    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("secretFoo");
    v2.setValue("secretBar");
    v2.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.SECRETSTRING);

    Set<Variable> results = underTest.setPipelineVariables("3", "1", v, v2);
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.STRING);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains secretFoo");
    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/1/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void setPipelineVariables_via_environment() throws Exception {
    Variable v = new Variable();
    v.setName("foo");
    v.setValue("bar");

    Variable v2 = new Variable();
    v2.setName("secretFoo");
    v2.setValue("secretBar");
    v2.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.SECRETSTRING);

    Pipeline pipeline = underTest.listPipelines("3", p -> p.getId().equals("1")).stream().findFirst().orElse(null);

    Set<Variable> results = pipeline.setVariables(v, v2);
    v.setType(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.STRING);
    v2.setValue(null);
    assertEquals(2, results.size(), "Response list correct size.");
    assertTrue(results.contains(v), "Results contains foo");
    assertTrue(results.contains(v2), "Results contains secretFoo");
    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/1/variables").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void status() {
    assertEquals(Pipeline.Status.fromValue("WAITING"), Pipeline.Status.WAITING);
    assertNull(Pipeline.Status.fromValue("foo"));
    assertEquals(Pipeline.Status.IDLE.getValue(), Pipeline.Status.IDLE.toString());
  }

  @Test
  void predicates() throws CloudManagerApiException {
    Collection<Pipeline> busy = underTest.listPipelines("3", Pipeline.IS_BUSY);
    assertEquals(2, busy.size());

    Collection<Pipeline> named = underTest.listPipelines("3", new Pipeline.NamePredicate("test1"));
    assertEquals(1, named.size());
  }

  private String buildPipeline1() throws IOException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    JsonGenerator gen = jsonFactory.createGenerator(writer);

    writeEditablePipeline(gen);

    gen.close();
    return writer.toString();
  }

  private void writeEditablePipeline(JsonGenerator gen) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("id", "1");
    gen.writeStringField("name", "test1");
    gen.writeStringField("status", "IDLE");
    gen.writeFieldName("phases");
    gen.writeStartArray();
    gen.writeStartObject();
    gen.writeStringField("name", "VALIDATE");
    gen.writeStringField("type", "VALIDATE");
    gen.writeEndObject();
    gen.writeStartObject();
    gen.writeStringField("name", "BUILD_1");
    gen.writeStringField("type", "BUILD");
    gen.writeStringField("repositoryId", pipeline1RepositoryId);
    gen.writeStringField("branch", pipeline1Branch);
    gen.writeEndObject();
    gen.writeStartObject();
    gen.writeEndObject();
    gen.writeEndArray();
    gen.writeFieldName("_links");
    gen.writeStartObject();
    writeLink(gen, "self", "/api/program/3/pipeline/1", false);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/execution", "/api/program/3/pipeline/1/execution", false);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/execution/id", "/api/program/3/pipeline/1/execution/{executionId}", true);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/variable", "/api/program/3/pipeline/1/variables", false);
    gen.writeEndObject();
    gen.writeEndObject();
  }

  private void writeLink(JsonGenerator gen, String name, String href, boolean templated) throws IOException {
    gen.writeFieldName(name);
    gen.writeStartObject();
    gen.writeStringField("href", href);
    if (templated) {
      gen.writeBooleanField("templated", true);
    }
    gen.writeEndObject();
  }
}

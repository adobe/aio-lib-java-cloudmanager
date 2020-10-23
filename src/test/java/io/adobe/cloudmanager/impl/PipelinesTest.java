package io.adobe.cloudmanager.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineUpdate;
import io.adobe.cloudmanager.model.Pipeline;
import io.adobe.cloudmanager.swagger.model.PipelinePhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class PipelinesTest extends AbstractApiTest {

  private String pipeline5Branch = "yellow";
  private String pipeline5RepositoryId = "1";

  @BeforeEach
  public void setupPipelinesForProgram5() {
    client.when(
        request().withMethod("GET").withPath("/api/program/5/pipelines")
    ).respond(
        request -> response().withBody(buildPipelines(), MediaType.APPLICATION_JSON)
    );
    client.when(
        request().withMethod("PATCH").withPath("/api/program/5/pipeline/5").withContentType(MediaType.APPLICATION_JSON)
    ).respond(this::handleGoodPatch);
  }

  private HttpResponse handleGoodPatch(HttpRequest request) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    io.adobe.cloudmanager.swagger.model.Pipeline pipeline = objectMapper.readValue(request.getBodyAsJsonOrXmlString(), io.adobe.cloudmanager.swagger.model.Pipeline.class);

    Optional<PipelinePhase> buildPhase = pipeline.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType()).findFirst();
    if (buildPhase.isPresent()) {
      if (buildPhase.get().getBranch() != null) {
        pipeline5Branch = buildPhase.get().getBranch();
      }
      if (buildPhase.get().getRepositoryId() != null) {
        pipeline5RepositoryId = buildPhase.get().getRepositoryId();
      }
    }

    return response().withBody(buildPipeline5(), MediaType.APPLICATION_JSON);
  }

  @Test
  void listPipelines_successEmpty() throws CloudManagerApiException  {
    List<Pipeline> pipelines = underTest.listPipelines("4");
    assertTrue(pipelines.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void listPipelines_success() throws CloudManagerApiException  {
    List<Pipeline> pipelines = underTest.listPipelines("5");
    assertEquals(4, pipelines.size(), "Correct pipelines list length");
  }

  @Test
  void listPipelines_programReturns404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("7"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve program: %s/api/program/7 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPipelines_programNotFound() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("8"), "Exception thrown for 404");
    assertEquals("Could not find program 8", exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_via_pipeline() throws Exception  {
    List<Pipeline> pipelines = underTest.listPipelines("5");
    Pipeline pipeline = pipelines.stream().filter(p -> p.getId().equals("5")).findFirst().orElseThrow(Exception::new);
    String executionUrl = pipeline.startExecution();
    assertEquals(String.format("%s/api/program/4/pipeline/8555/execution/12742", baseUrl), executionUrl, "URL was correct");
  }

  @Test
  void startExecution_badPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("5", "10"), "Exception thrown");
    assertEquals("Cannot start execution. Pipeline 10 does not exist in program 5.", exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_failed412() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("5", "6"), "Exception thrown");
    assertEquals("Cannot create execution. Pipeline already running.", exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_failed404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("5", "7"), "Exception thrown");
    assertEquals(String.format("Cannot create execution: %s/api/program/5/pipeline/7/execution (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_success() throws CloudManagerApiException{
    String executionUrl = underTest.startExecution("5", "5");
    assertEquals(String.format("%s/api/program/4/pipeline/8555/execution/12742", baseUrl), executionUrl, "URL was correct");
  }

  @Test
  void updatePipeline_badPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.updatePipeline("5", "10", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals("Pipeline 10 does not exist in program 5.", exception.getMessage(), "Message was correct");
  }

  @Test
  void updatePipeline_failure() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.updatePipeline("5", "8", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals(String.format("Cannot update pipeline: %s/api/program/5/pipeline/8 (405 Method Not Allowed)", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(request().withMethod("PATCH").withPath("/api/program/5/pipeline/8").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void updatePipeline_branchSuccess() throws CloudManagerApiException {
    Pipeline result = underTest.updatePipeline("5", "5", PipelineUpdate.builder().branch("develop").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("1");

    client.verify(request().withMethod("PATCH").withPath("/api/program/5/pipeline/5").withContentType(MediaType.APPLICATION_JSON));

    assertThat("update was successful", result.getPhases(), hasItem(expected));
  }

  @Test
  void updatePipeline_repositoryAndBranchSuccess() throws CloudManagerApiException {
    Pipeline result = underTest.updatePipeline("5", "5", PipelineUpdate.builder().branch("develop").repositoryId("4").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("4");

    client.verify(request().withMethod("PATCH").withPath("/api/program/5/pipeline/5").withContentType(MediaType.APPLICATION_JSON));

    assertThat("update was successful", result.getPhases(), hasItem(expected));
  }

  @Test
  void updatePipeline_via_pipeline() throws Exception  {
    List<Pipeline> pipelines = underTest.listPipelines("5");
    Pipeline pipeline = pipelines.stream().filter(p -> p.getId().equals("5")).findFirst().orElseThrow(Exception::new);
    Pipeline result = pipeline.update(PipelineUpdate.builder().branch("develop").repositoryId("4").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("4");

    client.verify(request().withMethod("PATCH").withPath("/api/program/5/pipeline/5").withContentType(MediaType.APPLICATION_JSON));

    assertThat("update was successful", result.getPhases(), hasItem(expected));
  }

  @Test
  void deletePipeline_returns400() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deletePipeline("5", "7"), "Exception was thrown");
    assertEquals(String.format("Cannot delete pipeline: %s/api/program/5/pipeline/7 (400 Bad Request)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void deletePipeline_badPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.deletePipeline("5", "10"));
    assertEquals("Pipeline 10 does not exist in program 5.", exception.getMessage(), "Message was correct");
  }

  @Test
  void deletePipeline_success() throws CloudManagerApiException {
    underTest.deletePipeline("5", "5");

    client.verify(request().withMethod("DELETE").withPath("/api/program/5/pipeline/5"));
  }

  @Test
  void deletePipeline_viaPipeline() throws Exception {
    List<Pipeline> pipelines = underTest.listPipelines("5");
    Pipeline pipeline = pipelines.stream().filter(p -> p.getId().equals("5")).findFirst().orElseThrow(Exception::new);

    pipeline.delete();

    client.verify(request().withMethod("DELETE").withPath("/api/program/5/pipeline/5"));
  }

  private String buildPipelines() throws IOException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    JsonGenerator gen = jsonFactory.createGenerator(writer);

    gen.writeStartObject();
    gen.writeFieldName("_embedded");
    gen.writeStartObject();
    gen.writeFieldName("pipelines");
    gen.writeStartArray();
    writeEditablePipeline(gen);
    writeStaticPipeline(gen, "6", "test2", "BUSY");
    writeStaticPipeline(gen, "7", "test3", "BUSY");
    writeStaticPipeline(gen, "8", "test4", "IDLE");
    gen.writeEndArray();
    gen.writeEndObject();
    gen.writeEndObject();

    gen.close();
    return writer.toString();
  }

  private String buildPipeline5() throws IOException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    JsonGenerator gen = jsonFactory.createGenerator(writer);

    writeEditablePipeline(gen);

    gen.close();
    return writer.toString();
  }

  private void writeEditablePipeline(JsonGenerator gen) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("id", "5");
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
    gen.writeStringField("repositoryId", pipeline5RepositoryId);
    gen.writeStringField("branch", pipeline5Branch);
    gen.writeEndObject();
    gen.writeStartObject();
    gen.writeEndObject();
    gen.writeEndArray();
    gen.writeFieldName("_links");
    gen.writeStartObject();
    writeLink(gen, "self", "/api/program/5/pipeline/5", false);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/execution", "/api/program/5/pipeline/5/execution", false);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/execution/id", "/api/program/5/pipeline/5/execution/{executionId}", true);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/variable", "/api/program/5/pipeline/5/variables", false);
    gen.writeEndObject();
    gen.writeEndObject();
  }

  private void writeStaticPipeline(JsonGenerator gen, String id, String name, String status) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("id", id);
    gen.writeStringField("name", name);
    gen.writeStringField("status", status);
    gen.writeFieldName("phases");
    gen.writeStartArray();
    gen.writeStartObject();
    gen.writeStringField("name", "VALIDATE");
    gen.writeStringField("type", "VALIDATE");
    gen.writeEndObject();
    gen.writeStartObject();
    gen.writeStringField("name", "BUILD_1");
    gen.writeStringField("type", "BUILD");
    gen.writeStringField("repositoryId", "1");
    gen.writeStringField("branch", "test");
    gen.writeEndObject();
    gen.writeStartObject();
    gen.writeEndObject();
    gen.writeEndArray();
    gen.writeFieldName("_links");
    gen.writeStartObject();
    writeLink(gen, "self", String.format("/api/program/5/pipeline/%s", id), false);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/execution", String.format("/api/program/5/pipeline/%s/execution", id), false);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/execution/id", String.format("/api/program/5/pipeline/%s/execution/{executionId}", id), true);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/variable", String.format("/api/program/5/pipeline/%s/variables", id), false);
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

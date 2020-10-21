package io.adobe.cloudmanager.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.model.Pipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class PipelinesTest extends AbstractApiTest {

  @BeforeEach
  public void setupPipelinesForProgram5() {
    client.when(
        request().withMethod("GET").withPath("/api/program/5/pipelines")
    ).respond(
        request -> response().withBody(buildPipelines(), MediaType.APPLICATION_JSON)
    );
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
    gen.writeStringField("repositoryId", "1");
    gen.writeStringField("branch", "yellow");
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

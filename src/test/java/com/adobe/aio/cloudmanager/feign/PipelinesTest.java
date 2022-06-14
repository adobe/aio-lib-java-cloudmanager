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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.PipelineUpdate;
import com.adobe.aio.cloudmanager.generated.model.PipelinePhase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.ClearType;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;

public class PipelinesTest extends AbstractApiClientTest {

  private String pipeline1Branch = "yellow";
  private String pipeline1RepositoryId = "1";

  public static List<String> getTestExpectationFiles() {
    return Arrays.asList(
        "pipelines/list-empty.json",
        "pipelines/list-success.json",
        "pipelines/delete-bad-request.json",
        "pipelines/delete-success.json",
        "pipelines/update-not-allowed.json",
        "pipelines/get-success.json"
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
    com.adobe.aio.cloudmanager.generated.model.Pipeline pipeline = objectMapper.readValue(request.getBodyAsJsonOrXmlString(), com.adobe.aio.cloudmanager.generated.model.Pipeline.class);

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

  @Test
  void listPipelines_failure404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("1"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve pipelines: %s/api/program/1/pipelines (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPipelines_successEmptyBody() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("6"), "Exception thrown for empty body");
    assertEquals(String.format("Could not find pipelines for program %s", "6"), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPipelines_successEmptyEmbedded() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("7"), "Exception thrown for no embedded");
    assertEquals(String.format("Could not find pipelines for program %s", "7"), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPipelines_successEmptyPipelines() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("8"), "Exception thrown for empty pipeline list");
    assertEquals(String.format("Could not find pipelines for program %s", "8"), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPipelines_success() throws CloudManagerApiException {
    Collection<Pipeline> pipelines = underTest.listPipelines("3");
    assertEquals(4, pipelines.size(), "Correct pipelines list length");
  }

  @Test
  void deletePipeline_failure400() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deletePipeline("3", "4"), "Exception thrown");
    assertEquals(String.format("Cannot delete pipeline: %s/api/program/3/pipeline/4 (400 Bad Request)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void deletePipeline_success() throws CloudManagerApiException {
    underTest.deletePipeline("3", "1");
    client.verify((request().withMethod("DELETE").withPath("/api/program/3/pipeline/1")));
    client.clear(request().withPath("/api/program/3/pipeline/1"), ClearType.LOG);
  }

  @Test
  void deletePipeline_viaPipeline() throws Exception {
    Collection<Pipeline> pipelines = underTest.listPipelines("3");
    Pipeline pipeline = pipelines.stream().filter(p -> p.getId().equals("1")).findFirst().orElseThrow(Exception::new);

    pipeline.delete();
    client.verify(request().withMethod("DELETE").withPath("/api/program/3/pipeline/1"));
    client.clear(request().withPath("/api/program/3/pipeline/1"), ClearType.LOG);
  }
  
  @Test
  void updatePipeline_failure404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.updatePipeline("3", "3", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals(String.format("Cannot retrieve pipeline: %s/api/program/3/pipeline/3 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }
  
  @Test
  void updatePipeline_failure() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.updatePipeline("3", "4", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals(String.format("Cannot update pipeline: %s/api/program/3/pipeline/4 (405 Method Not Allowed)", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(request().withMethod("PATCH").withPath("/api/program/3/pipeline/4").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/3/pipeline/4"), ClearType.LOG);
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
    client.clear(request().withPath("/api/program/3/pipeline/1"), ClearType.LOG);

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
    client.clear(request().withPath("/api/program/3/pipeline/1"), ClearType.LOG);

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
    client.clear(request().withPath("/api/program/3/pipeline/1"), ClearType.LOG);

  }
}

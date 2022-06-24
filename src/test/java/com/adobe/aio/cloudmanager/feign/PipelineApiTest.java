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
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.PipelineUpdate;
import com.adobe.aio.cloudmanager.impl.model.PipelinePhase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class PipelineApiTest extends AbstractApiClientTest {

  private static final JsonBody GET_BODY = loadBodyJson("pipeline/get.json");
  public static final JsonBody LIST_BODY = loadBodyJson("pipeline/list.json");

  private HttpResponse handleGoodPatch(HttpRequest request) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    com.adobe.aio.cloudmanager.impl.model.Pipeline pipeline = objectMapper.readValue(request.getBodyAsJsonOrXmlString(), com.adobe.aio.cloudmanager.impl.model.Pipeline.class);
    Optional<PipelinePhase> buildPhase = pipeline.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType()).findFirst();
    String branch = "yellow";
    String repositoryId = "1";
    
    if (buildPhase.isPresent()) {
      if (buildPhase.get().getBranch() != null) {
        branch = buildPhase.get().getBranch();
      }
      if (buildPhase.get().getRepositoryId() != null) {
        repositoryId = buildPhase.get().getRepositoryId();
      }
    }
    return response().withBody(buildPipeline(branch, repositoryId), MediaType.APPLICATION_JSON);
  }

  private String buildPipeline(String branch, String repoId) throws IOException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    JsonGenerator gen = jsonFactory.createGenerator(writer);

    writeEditablePipeline(gen, branch, repoId);

    gen.close();
    return writer.toString();
  }

  private void writeEditablePipeline(JsonGenerator gen, String branch, String repoId) throws IOException {
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
    gen.writeStringField("repositoryId", repoId);
    gen.writeStringField("branch", branch);
    gen.writeEndObject();
    gen.writeStartObject();
    gen.writeEndObject();
    gen.writeEndArray();
    gen.writeFieldName("_links");
    gen.writeStartObject();
    writeLink(gen, "self", "/api/program/1/pipeline/1", false);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/execution", "/api/program/1/pipeline/1/execution", false);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/execution/id", "/api/program/1/pipeline/1/execution/{executionId}", true);
    writeLink(gen, "http://ns.adobe.com/adobecloud/rel/variable", "/api/program/1/pipeline/1/variables", false);
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
  void list_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve pipelines: %s/api/program/1/pipelines (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
  }

  @Test
  void list_successEmptyBody() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(""));
    
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("1"), "Exception thrown.");
    assertEquals("Could not find pipelines for program 1.", exception.getMessage(), "Message was correct");
    
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
  }

  @Test
  void list_successEmptyEmbedded() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody("{}"));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("1"), "Exception thrown.");
    assertEquals("Could not find pipelines for program 1.", exception.getMessage(), "Message was correct");
    
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
  }

  @Test
  void list_successEmptyPipelines() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(json("{ \"_embedded\": {} }")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("1"), "Exception thrown.");
    assertEquals("Could not find pipelines for program 1.", exception.getMessage(), "Message was correct");
    
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(LIST_BODY));

    Collection<Pipeline> pipelines = underTest.listPipelines("1");
    assertEquals(4, pipelines.size(), "Correct pipelines list length");
    
    client.clear(list);
  }

  @Test
  void delete_failure400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deletePipeline("1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot delete pipeline: %s/api/program/1/pipeline/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void delete_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(NO_CONTENT_204.code()));

    underTest.deletePipeline("1", "1");
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void delete_via_pipeline() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(LIST_BODY));
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(NO_CONTENT_204.code()));

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    pipeline.delete();
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void update_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.updatePipeline("1", "1", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals(String.format("Cannot retrieve pipeline: %s/api/program/1/pipeline/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    
    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void update_failure() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1").withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(response().withStatusCode(METHOD_NOT_ALLOWED_405.code()));
    
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.updatePipeline("1", "1", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals(String.format("Cannot update pipeline: %s/api/program/1/pipeline/1 (405 Method Not Allowed).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(patch, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_branchSuccess() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));
    
    HttpRequest patch = request()
        .withPath("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1")
        .withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);
    
    Pipeline result = underTest.updatePipeline("1", "1", PipelineUpdate.builder().branch("develop").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("1");
    
    assertThat("update was successful", ((PipelineImpl) result).getPhases(), hasItem(expected));
    client.verify(patch, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(patch);
  }


  @Test
  void update_repositoryIdSuccess() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request()
        .withPath("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1")
        .withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);

    Pipeline result = underTest.updatePipeline("1", "1", PipelineUpdate.builder().repositoryId("4").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("yellow");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("4");

    assertThat("update was successful", ((PipelineImpl) result).getPhases(), hasItem(expected));
    client.verify(patch, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_repositoryAndBranchSuccess() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request()
        .withPath("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1")
        .withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);

    Pipeline result = underTest.updatePipeline("1", "1", PipelineUpdate.builder().branch("develop").repositoryId("4").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("4");

    assertThat("update was successful", ((PipelineImpl) result).getPhases(), hasItem(expected));
    
    client.verify(patch, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_via_pipeline() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withBody(LIST_BODY));
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request()
        .withPath("PATCH")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1")
        .withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    Pipeline result = pipeline.update(PipelineUpdate.builder().branch("develop").repositoryId("4").build());

    PipelinePhase expected = new PipelinePhase();
    expected.setName("BUILD_1");
    expected.setBranch("develop");
    expected.setType(PipelinePhase.TypeEnum.BUILD);
    expected.setRepositoryId("4");
    assertThat("update was successful", ((PipelineImpl) result).getPhases(), hasItem(expected));

    client.verify(patch, VerificationTimes.exactly(1));
    client.clear(list);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void invalidate_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/cache");
    client.when(delete).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.invalidatePipelineCache("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot invalidate pipeline cache: %s/api/program/1/pipeline/1/cache (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void invalidate_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/list.json")));

    HttpRequest delete = request().withMethod("DELETE").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/cache");
    client.when(delete).respond(response().withStatusCode(ACCEPTED_202.code()));
    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    underTest.invalidatePipelineCache(pipeline);
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(delete);

  }

  @Test
  void status() {
    assertEquals(Pipeline.Status.fromValue("WAITING"), Pipeline.Status.WAITING);
    assertNull(Pipeline.Status.fromValue("foo"));
    assertEquals(Pipeline.Status.IDLE.getValue(), Pipeline.Status.IDLE.toString());
  }

  @Test
  void predicates() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<Pipeline> busy = underTest.listPipelines("1", Pipeline.IS_BUSY);
    assertEquals(2, busy.size());

    Collection<Pipeline> named = underTest.listPipelines("1", new Pipeline.NamePredicate("test1"));
    assertEquals(1, named.size());
  }
}

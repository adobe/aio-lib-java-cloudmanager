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
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineUpdate;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.impl.generated.EmbeddedProgram;
import io.adobe.cloudmanager.impl.generated.PipelinePhase;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

class PipelineTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("pipeline/get.json");
  public static final JsonBody LIST_BODY = loadBodyJson("pipeline/list.json");

  private HttpResponse handleGoodPatch(HttpRequest request) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream("pipeline/get.json")) {
      io.adobe.cloudmanager.impl.generated.Pipeline original = objectMapper.readValue(is, io.adobe.cloudmanager.impl.generated.Pipeline.class);
      PipelinePhase oBuildPhase = original.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType()).findFirst().orElse(null);
      assert oBuildPhase != null;

      io.adobe.cloudmanager.impl.generated.Pipeline pipeline = objectMapper.readValue(request.getBodyAsJsonOrXmlString(), io.adobe.cloudmanager.impl.generated.Pipeline.class);
      PipelinePhase uBuildPhase = pipeline.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType()).findFirst().orElse(null);
      assert uBuildPhase != null;
      if (uBuildPhase.getBranch() != null) {
        oBuildPhase.setBranch(uBuildPhase.getBranch());
      }
      if (uBuildPhase.getRepositoryId() != null) {
        oBuildPhase.setRepositoryId(uBuildPhase.getRepositoryId());
      }
      return response().withBody(objectMapper.writeValueAsString(original), MediaType.APPLICATION_JSON);
    }
  }

  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve pipelines: %s/api/program/1/pipelines (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_emptyBody() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(""));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("1"), "Exception thrown.");
    assertEquals("Could not find pipelines for program 1.", exception.getMessage(), "Message was correct");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_emptyPipelines() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(json("{ \"_embedded\": {} }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPipelines("1"), "Exception thrown.");
    assertEquals("Could not find pipelines for program 1.", exception.getMessage(), "Message was correct");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(LIST_BODY));
    Collection<Pipeline> pipelines = underTest.listPipelines("1");
    assertEquals(4, pipelines.size(), "Correct pipelines list length");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success_viaProgram(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(LIST_BODY));
    Program program = new ProgramImpl(mock, underTest);
    Collection<Pipeline> pipelines = program.listPipelines();
    assertEquals(4, pipelines.size(), "Correct pipelines list length");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getPipeline("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot retrieve pipeline: %s/api/program/1/pipeline/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));
    Pipeline pipeline = underTest.getPipeline("1", "1");
    assertEquals("1", pipeline.getId());

    client.verify(get);
    client.clear(get);
  }

  @Test
  void delete_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deletePipeline("1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot delete pipeline: %s/api/program/1/pipeline/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(delete);
    client.clear(delete);
  }

  @Test
  void delete_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(NO_CONTENT_204.code()));
    underTest.deletePipeline("1", "1");
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void delete_success_viaPipeline(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(NO_CONTENT_204.code()));
    new PipelineImpl(mock, underTest).delete();
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void update_failure_no_build_phase() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/get_no_build.json")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.updatePipeline("1", "1", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals(String.format("Pipeline %s does not appear to have a build phase.", "1"), exception.getMessage(), "Message was correct");
    client.verify(get, VerificationTimes.once());
    client.clear(get);
  }

  @Test
  void update_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1").withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.updatePipeline("1", "1", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals(String.format("Cannot update pipeline: %s/api/program/1/pipeline/1 (400 Bad Request).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.once());
    client.verify(patch);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_branch_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1").withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);
    PipelineUpdate updates = PipelineUpdate.builder().branch("newbranch").build();
    PipelineImpl result = (PipelineImpl) underTest.updatePipeline("1", "1", updates);
    assertEquals("newbranch", result.getPhases().get(1).getBranch(), "Branch was set correctly.");
    client.verify(get, VerificationTimes.once());
    client.verify(patch);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_repository_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1").withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);
    PipelineUpdate updates = PipelineUpdate.builder().repositoryId("2").build();
    PipelineImpl result = (PipelineImpl) underTest.updatePipeline("1", "1", updates);
    assertEquals("2", result.getPhases().get(1).getRepositoryId(), "Branch was set correctly.");
    client.verify(get, VerificationTimes.once());
    client.verify(patch);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_via_pipeline(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1").withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);
    PipelineUpdate updates = PipelineUpdate.builder().branch("newbranch").repositoryId("2").build();
    PipelineImpl result = (PipelineImpl) new PipelineImpl(mock, underTest).update(updates);

    assertEquals("newbranch", result.getPhases().get(1).getBranch(), "Branch was set correctly.");
    assertEquals("2", result.getPhases().get(1).getRepositoryId(), "Branch was set correctly.");

    client.verify(get, VerificationTimes.once());
    client.verify(patch);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void invalidate_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/cache");
    client.when(delete).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.invalidatePipelineCache("1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot invalidate pipeline cache: %s/api/program/1/pipeline/1/cache (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(delete);
    client.clear(delete);
  }

  @Test
  void invalidate_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/cache");
    client.when(delete).respond(response().withStatusCode(ACCEPTED_202.code()));
    underTest.invalidatePipelineCache("1", "1");
    client.verify(delete, VerificationTimes.once());
    client.clear(delete);
  }

  @Test
  void invalidate_success_viaPipeline(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/cache");
    client.when(delete).respond(response().withStatusCode(ACCEPTED_202.code()));
    new PipelineImpl(mock, underTest).invalidateCache();
    client.verify(delete, VerificationTimes.once());
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
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<Pipeline> busy = underTest.listPipelines("1", Pipeline.IS_BUSY);
    assertEquals(2, busy.size());
    Collection<Pipeline> named = underTest.listPipelines("1", new Pipeline.NamePredicate("test1"));
    assertEquals(1, named.size());
    Collection<Pipeline> id = underTest.listPipelines("1", new Pipeline.IdPredicate("4"));
    assertEquals(1, id.size());

    client.verify(list, VerificationTimes.exactly(3));
    client.clear(list);
  }
}

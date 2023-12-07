package com.adobe.aio.cloudmanager.impl.pipeline.execution;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
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
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.adobe.aio.cloudmanager.impl.generated.Pipeline;
import com.adobe.aio.cloudmanager.impl.generated.PipelineExecution;
import com.adobe.aio.event.webhook.service.EventVerifier;
import com.adobe.aio.ims.feign.AuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.adobe.aio.cloudmanager.ApiBuilder;
import com.adobe.aio.cloudmanager.Artifact;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Metric;
import com.adobe.aio.cloudmanager.PipelineApi;
import com.adobe.aio.cloudmanager.PipelineExecutionApi;
import com.adobe.aio.cloudmanager.PipelineExecutionEndEvent;
import com.adobe.aio.cloudmanager.PipelineExecutionStartEvent;
import com.adobe.aio.cloudmanager.PipelineExecutionStepEndEvent;
import com.adobe.aio.cloudmanager.PipelineExecutionStepStartEvent;
import com.adobe.aio.cloudmanager.PipelineExecutionStepState;
import com.adobe.aio.cloudmanager.PipelineExecutionStepWaitingEvent;
import com.adobe.aio.cloudmanager.StepAction;
import com.adobe.aio.cloudmanager.impl.AbstractApiTest;
import com.adobe.aio.cloudmanager.impl.pipeline.PipelineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;
import org.mockserver.verify.VerificationTimes;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class PipelineExecutionTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("pipeline/execution/get.json");
  private static final JsonBody GET_STEP_BODY = loadBodyJson("pipeline/execution/get-step.json");
  private static final JsonBody GET_WAITING_BODY = loadBodyJson("pipeline/execution/approval-waiting.json");
  private static final JsonBody GET_CODE_QUALITY_BODY = loadBodyJson("pipeline/execution/codeQuality-waiting.json");
  public static final JsonBody LIST_BODY = loadBodyJson("pipeline/execution/list.json");

  private PipelineApi pipelineApi;
  private PipelineExecutionApiImpl executionApi;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      pipelineApi = new ApiBuilder<>(PipelineApi.class).workspace(workspace).url(new URL(baseUrl)).build();
      executionApi = (PipelineExecutionApiImpl) new ApiBuilder<>(PipelineExecutionApi.class).workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  private HttpRequest setupDownloadUrl(String sessionId, String path) {
    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath(path);
    client.when(redirect).respond(
        response()
            .withStatusCode(OK_200.code())
            .withBody(json(String.format("{ \"redirect\": \"%s/logs/special.txt\" }", baseUrl)))
    );
    return redirect;
  }

  private HttpRequest setupDownloadUrlSpecial(String sessionId) {
    HttpRequest redirect = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs")
        .withQueryStringParameter("file", "somethingspecial");
    client.when(redirect).respond(
        response()
            .withStatusCode(OK_200.code())
            .withBody(String.format("{ \"redirect\": \"%s/logs/somethingspecial.txt\" }", baseUrl))
    );
    return redirect;
  }

  @Test
  void current_failure_404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    assertFalse(executionApi.getCurrent("1", "1").isPresent(), "Correct state");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void current_failure_500() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withStatusCode(INTERNAL_SERVER_ERROR_500.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getCurrent("1", "1"), "Exception was thrown.");
    assertEquals(String.format("Cannot get execution: %s/api/program/1/pipeline/1/execution (500 Unknown).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void current_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));

    Optional<com.adobe.aio.cloudmanager.PipelineExecution> opt = executionApi.getCurrent("1", "1");

    assertTrue(opt.isPresent(), "Execution found.");
    com.adobe.aio.cloudmanager.PipelineExecution exec = opt.get();
    assertEquals("1", exec.getId(), "Id Matches");
    assertEquals("1", exec.getProgramId(), "Program Id Matches");
    assertEquals("1", exec.getPipelineId(), "Pipeline Id Matches");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void current_success_via_pipeline(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));

    com.adobe.aio.cloudmanager.Pipeline pipeline = new PipelineImpl(mock, pipelineApi, executionApi);
    Optional<com.adobe.aio.cloudmanager.PipelineExecution> opt = pipeline.getCurrentExecution();

    assertTrue(opt.isPresent(), "Execution found.");
    com.adobe.aio.cloudmanager.PipelineExecution exec = opt.get();
    assertEquals("1", exec.getId(), "Id Matches");
    assertEquals("1", exec.getProgramId(), "Program Id Matches");
    assertEquals("1", exec.getPipelineId(), "Pipeline Id Matches");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void start_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.start("1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot create execution: %s/api/program/1/pipeline/1/execution (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(put);
    client.clear(put);
  }

  @Test
  void start_failure_412() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(PRECONDITION_FAILED_412.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.start("1", "1"), "Exception thrown");
    assertEquals("Cannot create execution. Pipeline already running.", exception.getMessage(), "Message was correct");
    client.verify(put);
    client.clear(put);
  }

  @Test
  void start_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(CREATED_201.code()).withBody(GET_BODY));

    com.adobe.aio.cloudmanager.PipelineExecution execution = executionApi.start("1", "1");
    assertNotNull(execution);
    client.verify(put);
    client.clear(put);
  }

  @Test
  void start_success_via_pipeline(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(CREATED_201.code()).withBody(GET_BODY));

    com.adobe.aio.cloudmanager.Pipeline p = new PipelineImpl(mock, pipelineApi, executionApi);
    com.adobe.aio.cloudmanager.PipelineExecution execution = p.startExecution();
    assertNotNull(execution);
    client.verify(put);
    client.clear(put);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.get("1", "1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot get execution: %s/api/program/1/pipeline/1/execution/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    com.adobe.aio.cloudmanager.PipelineExecution exec = executionApi.get("1", "1", "1");
    assertNotNull(exec);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success_via_pipeline(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    com.adobe.aio.cloudmanager.PipelineExecution exec = new PipelineImpl(mock, pipelineApi, executionApi).getExecution("1");
    assertNotNull(exec);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getStepState_failure_nostep(@Mock PipelineExecution mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getStepState(new PipelineExecutionImpl(mock, executionApi), StepAction.deploy), "Exception thrown.");
    assertEquals("Cannot find step state for action 'deploy' on execution 1.", exception.getMessage(), "Message was correct");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getStepState_empty(@Mock PipelineExecution mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(json("{}")));
    assertThrows(CloudManagerApiException.class, () -> executionApi.getStepState(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality), "Exception thrown.");
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertThrows(CloudManagerApiException.class, () -> executionApi.getStepState(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality), "Exception thrown.");
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{ \"_embedded\": { \"stepStates\": [] } }")));
    assertThrows(CloudManagerApiException.class, () -> executionApi.getStepState(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality), "Exception thrown.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getStepState_success(@Mock PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecutionStepState stepState = executionApi.getStepState(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality);
    assertNotNull(stepState);
    assertEquals(PipelineExecutionStepState.Status.NOT_STARTED, stepState.getStatusState());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getStepState_via_execution() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withBody(LIST_BODY));

    final List<com.adobe.aio.cloudmanager.PipelineExecution> executions = new ArrayList<>(executionApi.list("1", "1"));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executions.get(0).getStep(StepAction.deploy));
    assertEquals("Cannot find step with action 'deploy' for pipeline 1, execution 1.", exception.getMessage(), "Message was correct.");

    exception = assertThrows(CloudManagerApiException.class, () -> executions.get(19).getStep(StepAction.build), "Exception thrown.");
    assertEquals("Cannot find step with action 'build' for pipeline 1, execution 20.", exception.getMessage(), "Message was correct.");

    com.adobe.aio.cloudmanager.PipelineExecution execution = executions.get(0);
    assertEquals(StepAction.build, execution.getStep(StepAction.build).getStepAction(), "Correct step found.");

    client.verify(list, VerificationTimes.once());
    client.clear(list);
  }

  @Test
  void getCurrentStep_via_execution(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withBody(LIST_BODY));

    List<com.adobe.aio.cloudmanager.PipelineExecution> executions = new ArrayList<>(executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi)));
    com.adobe.aio.cloudmanager.PipelineExecution execution = executions.get(0);
    assertEquals(StepAction.build, execution.getCurrentStep().getStepAction(), "Correct step found.");

    // No running step.
    execution = executions.get(1);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, execution::getCurrentStep, "Exception thrown.");
    assertEquals("Cannot find a current step for pipeline 1, execution 2.", exception.getMessage(), "Message was correct.");

    // No steps
    execution = executions.get(19);
    exception = assertThrows(CloudManagerApiException.class, execution::getCurrentStep, "Exception thrown.");
    assertEquals("Cannot find a current step for pipeline 1, execution 20.", exception.getMessage(), "Message was correct.");

    client.verify(list, VerificationTimes.once());
    client.clear(list);
  }

  @Test
  void advance_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance")
        .withBody(new JsonBody("{ \"approved\": true }"));
    client.when(put).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.advance("1", "1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot advance execution: %s/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void advance_failure_buildRunning() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.advance("1", "1", "1"), "Exception thrown");
    assertEquals("Cannot find a waiting step for pipeline 1, execution 1.", exception.getMessage(), "Message was correct");

    client.verify(get);
    client.clear(get);
  }

  @Test
  void advance_approval_waiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance")
        .withBody(new JsonBody("{ \"approved\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.advance("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void advance_codeQuality_waiting(@Mock PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));

    HttpRequest metrics = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(metrics).respond(response().withBody(loadBodyJson("pipeline/execution/codeQuality-metrics.json")));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/advance")
        .withBody(loadBodyJson("pipeline/execution/put-metrics-override.json"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.advance(new PipelineExecutionImpl(mock, executionApi));
    client.verify(get, metrics, put);
    client.clear(get);
    client.clear(metrics);
    client.clear(put);
  }

  @Test
  void advance_via_execution() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    ObjectMapper objectMapper = new ObjectMapper();

    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream("pipeline/execution/approval-waiting.json")) {
      PipelineExecution original = objectMapper.readValue(is, PipelineExecution.class);

      HttpRequest put = request().withMethod("PUT")
          .withHeader(API_KEY_HEADER, sessionId)
          .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance")
          .withBody(new JsonBody("{ \"approved\": true }"));
      client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

      new PipelineExecutionImpl(original, executionApi).advance();
      client.verify(put);
      client.clear(put);
    }
  }

  @Test
  void cancel_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel")
        .withBody(new JsonBody("{ \"approved\": false }"));
    client.when(put).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.cancel("1", "1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot cancel execution: %s/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_failure_nostep() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/execution/no-active.json")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.cancel("1", "1", "1"), "Exception thrown");
    assertEquals("Cannot find a cancelable step for pipeline 1, execution 1.", exception.getMessage(), "Message was correct");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void cancel_build_running() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel")
        .withBody(json("{ \"cancel\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_approval_waiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel")
        .withBody(new JsonBody("{ \"approved\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_codeQuality_waiting(@Mock PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/cancel")
        .withBody(json("{ \"override\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel(new PipelineExecutionImpl(mock, executionApi));
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_schedule_waiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/execution/schedule-waiting.json")));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/2/cancel")
        .withBody(json("{ \"cancel\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_deploy_waiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/execution/deploy-waiting.json")));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/2/cancel")
        .withBody(json("{ \"resume\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_via_execution() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    ObjectMapper objectMapper = new ObjectMapper();

    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream("pipeline/execution/approval-waiting.json")) {
      PipelineExecution original = objectMapper.readValue(is, PipelineExecution.class);

      HttpRequest put = request().withMethod("PUT")
          .withHeader(API_KEY_HEADER, sessionId)
          .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel")
          .withBody(new JsonBody("{ \"approved\": false }"));
      client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

      new PipelineExecutionImpl(original, executionApi).cancel();
      client.verify(put);
      client.clear(put);
    }
  }

  @Test
  void getStepLogDownloadUrl_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(redirect).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getStepLogDownloadUrl("1", "1", "1", StepAction.build), "Exception thrown");
    assertEquals(String.format("Cannot get logs: %s/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getStepLogDownloadUrl_no_redirect() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/logs");
    client.when(redirect).respond(response().withStatusCode(OK_200.code()).withBody(""));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getStepLogDownloadUrl("1", "1", "1", StepAction.validate), "Exception thrown");
    assertEquals("Log redirect for execution 1, action 'validate' did not exist.", exception.getMessage(), "Message was correct.");
    client.verify(redirect);
    client.clear(redirect);

    client.when(redirect).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));
    exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getStepLogDownloadUrl("1", "1", "1", StepAction.validate), "Exception thrown");
    assertEquals("Log redirect for execution 1, action 'validate' did not exist.", exception.getMessage(), "Message was correct.");
    client.verify(redirect);
    client.clear(redirect);


    client.verify(get, VerificationTimes.exactly(2));
    client.clear(get);

  }

  @Test
  void getStepLogDownloadUrl_success(@Mock PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = setupDownloadUrl(sessionId, "/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");

    com.adobe.aio.cloudmanager.PipelineExecution execution = new PipelineExecutionImpl(mock, executionApi);
    assertEquals(String.format("%s/logs/special.txt", baseUrl), executionApi.getStepLogDownloadUrl(execution, StepAction.build));
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getStepLogDownloadUrl_success_alternateFile(@Mock PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = setupDownloadUrlSpecial(sessionId);
    com.adobe.aio.cloudmanager.PipelineExecution execution = new PipelineExecutionImpl(mock, executionApi);

    assertEquals(String.format("%s/logs/somethingspecial.txt", baseUrl), executionApi.getStepLogDownloadUrl(execution, StepAction.build, "somethingspecial"));
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getMetrics_failure_403(@Mock PipelineExecution mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest exec = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(exec).respond(response().withBody(GET_BODY));

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(get).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getQualityGateResults(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality), "Exception thrown.");
    assertEquals(String.format("Cannot get metrics: %s/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");

    client.verify(exec, get);
    client.clear(exec);
    client.clear(get);
  }

  @Test
  void getMetrics_empty(@Mock PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest exec = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(exec).respond(response().withBody(GET_BODY));

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(get).respond(response().withBody(json("{}")));
    Collection<Metric> metrics = executionApi.getQualityGateResults(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality);
    assertTrue(metrics.isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{ \"metrics\": [] }")));
    metrics = executionApi.getQualityGateResults(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality);
    assertTrue(metrics.isEmpty());
    client.verify(get);
    client.clear(get);

    client.verify(exec, VerificationTimes.exactly(2));
    client.clear(exec);
  }

  @Test
  void getMetrics_success(@Mock PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest exec = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(exec).respond(response().withBody(GET_BODY));

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/execution/codeQuality-metrics.json")));

    Collection<Metric> metrics = executionApi.getQualityGateResults(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality);
    assertEquals(8, metrics.size());

    client.verify(exec, get);
    client.clear(exec);
    client.clear(get);
  }

  @Test
  void list_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.list("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot list executions: %s/api/program/1/pipeline/1/executions (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_empty(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");

    client.when(list).respond(response().withBody(json("{}")));
    Collection<com.adobe.aio.cloudmanager.PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi));
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi));
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"executions\": [] } }")));
    executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi));
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<com.adobe.aio.cloudmanager.PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi));
    assertEquals(20, executions.size(), "Correct length.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_limit_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.list("1", "1", 10), "Exception thrown.");
    assertEquals(String.format("Cannot list executions: %s/api/program/1/pipeline/1/executions?start=0&limit=10 (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_limit_empty(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "30");

    client.when(list).respond(response().withBody(json("{}")));
    Collection<com.adobe.aio.cloudmanager.PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 30);
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 30);
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"executions\": [] } }")));
    executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 30);
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_limit_success(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "30");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<com.adobe.aio.cloudmanager.PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 30);
    assertEquals(20, executions.size(), "Correct length.");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_start_limit_empty(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");

    client.when(list).respond(response().withBody(json("{}")));
    Collection<com.adobe.aio.cloudmanager.PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 10, 10);
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 10, 10);
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"executions\": [] } }")));
    executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 10, 10);
    assertTrue(executions.isEmpty());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_start_limit_success(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "30");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<com.adobe.aio.cloudmanager.PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 10, 30);
    assertEquals(20, executions.size(), "Correct length.");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void listArtifacts_failure_403(@Mock com.adobe.aio.cloudmanager.PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifacts");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.listArtifacts(step), "Exception thrown.");
    assertEquals(String.format("Cannot list step artifacts: %s/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifacts (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listArtifacts_empty(@Mock com.adobe.aio.cloudmanager.PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifacts");

    client.when(list).respond(response().withBody(json("{}")));
    Collection<Artifact> artifacts = executionApi.listArtifacts(step);
    assertTrue(artifacts.isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    artifacts = executionApi.listArtifacts(step);
    assertTrue(artifacts.isEmpty());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listArtifacts_success(@Mock com.adobe.aio.cloudmanager.PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifacts");
    client.when(list).respond(response().withBody(loadBodyJson("pipeline/execution/list-artifacts.json")));
    Collection<Artifact> artifacts = executionApi.listArtifacts(step);
    assertEquals(1, artifacts.size(), "Collection size correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void getArtifactDownloadUrl_failure_404(@Mock com.adobe.aio.cloudmanager.PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifact/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getArtifactDownloadUrl(step, "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get step artifact: %s/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifact/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getArtifactDownloadUrl_no_redirect(@Mock com.adobe.aio.cloudmanager.PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifact/1");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getArtifactDownloadUrl(step, "1"), "Exception thrown");
    assertEquals("Artifact redirect for execution 1, phase 1, step 1 did not exist.", exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getArtifactDownloadUrl_success(@Mock com.adobe.aio.cloudmanager.PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");
    HttpRequest get = setupDownloadUrl(sessionId, "/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifact/1");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));
    String redirect = executionApi.getArtifactDownloadUrl(step, "1");
    assertEquals(String.format("%s/logs/special.txt", baseUrl), redirect, "Response was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void isRunning(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withBody(LIST_BODY));

    List<com.adobe.aio.cloudmanager.PipelineExecution> executions = new ArrayList<>(executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi)));
    assertTrue(executions.get(0).isRunning());
    assertFalse(executions.get(1).isRunning());
    client.verify(list, VerificationTimes.once());
    client.clear(list);
  }

  @Test
  void downloadStepLog_redirect_failure_404() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest getRedirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/build.txt\" }", baseUrl))));

    HttpRequest getFile = request().withMethod("GET").withPath("/logs/build.txt");
    client.when(getFile).respond(response().withStatusCode(NOT_FOUND_404.code()));

    final com.adobe.aio.cloudmanager.PipelineExecution exec = executionApi.get("1", "1", "1");
    final PipelineExecutionStepState step = exec.getStep(StepAction.build);
    final File outputDir = Files.createTempDirectory("log-output").toFile();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> step.getLog(outputDir), "Exception thrown.");
    assertEquals(String.format("Cannot download log for pipeline 1, execution 1, step 'build' to %s/pipeline-1-execution-1-build.txt (Cause: java.io.FileNotFoundException).", outputDir), exception.getMessage(), "Message was correct");

    client.verify(get);
    client.verify(getRedirect);
    client.verify(getFile);
    client.clear(get);
    client.clear(getRedirect);
    client.clear(getFile);
  }

  @Test
  void downloadStepLog_success() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest getRedirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/build.txt\" }", baseUrl))));

    HttpRequest getFile = request().withMethod("GET").withPath("/logs/build.txt");
    client.when(getFile).respond(response().withBody("some log line\nsome other log line\n"));

    final com.adobe.aio.cloudmanager.PipelineExecution exec = executionApi.get("1", "1", "1");
    final PipelineExecutionStepState step = exec.getStep(StepAction.build);
    final File outputDir = Files.createTempDirectory("log-output").toFile();

    step.getLog(outputDir);
    assertTrue(FileUtils.sizeOf(new File(outputDir, "pipeline-1-execution-1-build.txt")) > 0, "File is not empty.");

    client.verify(get);
    client.verify(getRedirect);
    client.verify(getFile);
    client.clear(get);
    client.clear(getRedirect);
    client.clear(getFile);
  }

  @Test
  void downloadStepLog_namedFile_redirect_failure_404() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest getRedirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs").withQueryStringParameter("file", "named");
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/build-special.txt\" }", baseUrl))));

    HttpRequest getFile = request().withMethod("GET").withPath("/logs/build-special.txt");
    client.when(getFile).respond(response().withStatusCode(NOT_FOUND_404.code()));

    final com.adobe.aio.cloudmanager.PipelineExecution exec = executionApi.get("1", "1", "1");
    final PipelineExecutionStepState step = exec.getStep(StepAction.build);
    final File outputDir = Files.createTempDirectory("log-output").toFile();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> step.getLog("named", outputDir), "Exception thrown.");
    assertEquals(String.format("Cannot download 'named' log for pipeline 1, execution 1, step 'build' to %s/pipeline-1-execution-1-build-named.txt (Cause: java.io.FileNotFoundException).", outputDir, baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get);
    client.verify(getRedirect);
    client.verify(getFile);
    client.clear(get);
    client.clear(getRedirect);
    client.clear(getFile);
  }

  @Test
  void downloadStepLog_named_success() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest getRedirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs").withQueryStringParameter("file", "named");
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/build.txt\" }", baseUrl))));

    HttpRequest getFile = request().withMethod("GET").withPath("/logs/build.txt");
    client.when(getFile).respond(response().withBody("some log line\nsome other log line\n"));

    final com.adobe.aio.cloudmanager.PipelineExecution exec = executionApi.get("1", "1", "1");
    final PipelineExecutionStepState step = exec.getStep(StepAction.build);
    final File outputDir = Files.createTempDirectory("log-output").toFile();

    step.getLog("named", outputDir);
    assertTrue(FileUtils.sizeOf(new File(outputDir, "pipeline-1-execution-1-build-named.txt")) > 0, "File is not empty.");

    client.verify(get);
    client.verify(getRedirect);
    client.verify(getFile);
    client.clear(get);
    client.clear(getRedirect);
    client.clear(getFile);
  }

  @Test
  void parseEvent_invalid() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.parseEvent("", new HashMap<>()), "Exception thrown.");
    assertEquals("Cannot parse event, did not pass signature validation.", exception.getMessage(), "Message was correct.");
  }

  @Test
  void parseEvent_valid() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    String body = IOUtils.resourceToString("pipeline/execution/event/pipeline-start.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    Map<String, String> headers = new HashMap<>();

    try (
        MockedConstruction<AuthInterceptor.Builder> builder = mockConstruction(AuthInterceptor.Builder.class,
            (mock, mockContext) -> {
              when(mock.workspace(workspace)).thenReturn(mock);
              when(mock.build()).thenReturn(authInterceptor);
            }
        );
        MockedConstruction<EventVerifier> verifier = mockConstruction(EventVerifier.class, (mock, mockContext) -> {
              when(mock.verify(body, sessionId, headers)).thenReturn(true);
            }
        )) {
      PipelineExecutionApi api = new ApiBuilder<>(PipelineExecutionApi.class).workspace(workspace).url(new URL(baseUrl)).build();
      PipelineExecutionStartEvent event = (PipelineExecutionStartEvent) api.parseEvent(body, headers);
      assertNotNull(event);
    }
  }

  @Test
  void parseEvent_unknown() throws IOException {
    String unknown = IOUtils.resourceToString("pipeline/execution/event/unknown.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.parseEvent(unknown), "Exception thrown.");
    assertEquals("Unknown event/object types (Event: 'https://ns.adobe.com/experience/cloudmanager/event/unknown', Object: 'https://ns.adobe.com/experience/cloudmanager/pipeline').", exception.getMessage(), "Message was correct.");

    String unknownObj = IOUtils.resourceToString("pipeline/execution/event/unknown-object.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    exception = assertThrows(CloudManagerApiException.class, () -> executionApi.parseEvent(unknownObj), "Exception thrown.");
    assertEquals("Unknown event/object types (Event: 'https://ns.adobe.com/experience/cloudmanager/event/started', Object: 'https://ns.adobe.com/experience/cloudmanager/pipeline').", exception.getMessage(), "Message was correct.");

    String unknownEvent = IOUtils.resourceToString("pipeline/execution/event/unknown-event.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    exception = assertThrows(CloudManagerApiException.class, () -> executionApi.parseEvent(unknownEvent), "Exception thrown.");
    assertEquals("Unknown event/object types (Event: 'https://ns.adobe.com/experience/cloudmanager/event/unknown', Object: 'https://ns.adobe.com/experience/cloudmanager/execution-step-state').", exception.getMessage(), "Message was correct.");
  }

  @Test
  void parseEvent_pipelineStart() throws IOException, CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    // Parse
    String body = IOUtils.resourceToString("pipeline/execution/event/pipeline-start.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    PipelineExecutionStartEvent event = (PipelineExecutionStartEvent) executionApi.parseEvent(body);
    assertNotNull(event);

    // Execution Detail Fetch
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    assertNotNull(event.getExecution());
    client.verify(get);
    client.clear(get);

    // Execution detail not found
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, event::getExecution, "Exception thrown.");
    assertEquals(String.format("Cannot get execution: %s/api/program/1/pipeline/1/execution/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void parseEvent_pipelineStepStart() throws IOException, CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    // Parse
    String body = IOUtils.resourceToString("pipeline/execution/event/pipeline-step-start.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    PipelineExecutionStepStartEvent event = (PipelineExecutionStepStartEvent) executionApi.parseEvent(body);
    assertNotNull(event);

    // Step State detail fetch
    HttpRequest getStep = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(getStep).respond(response().withBody(GET_STEP_BODY));
    client.when(get).respond(response().withBody(GET_BODY));
    assertNotNull(event.getStepState());
    client.verify(getStep, VerificationTimes.once());
    client.verify(get, VerificationTimes.once());
    client.clear(getStep);
    client.clear(get);

    // Step State not found
    getStep = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1");
    client.when(getStep).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, event::getStepState, "Exception thrown.");
    assertEquals(String.format("Cannot get execution step state: %s/api/program/1/pipeline/1/execution/1/phase/1/step/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");

    client.verify(getStep, VerificationTimes.once());
    client.verify(get, VerificationTimes.never());
    client.clear(getStep);
    client.clear(get);
  }

  @Test
  void parseEvent_pipelineStepWaiting() throws IOException, CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    // Parse
    String body = IOUtils.resourceToString("pipeline/execution/event/pipeline-step-waiting.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    PipelineExecutionStepWaitingEvent event = (PipelineExecutionStepWaitingEvent) executionApi.parseEvent(body);
    assertNotNull(event);

    // Step State detail fetch
    HttpRequest getStep = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(getStep).respond(response().withBody(GET_STEP_BODY));
    client.when(get).respond(response().withBody(GET_BODY));
    assertNotNull(event.getStepState());
    client.verify(getStep, VerificationTimes.once());
    client.verify(get, VerificationTimes.once());
    client.clear(getStep);
    client.clear(get);

    // Step State not found
    getStep = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1");
    client.when(getStep).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, event::getStepState, "Exception thrown.");
    assertEquals(String.format("Cannot get execution step state: %s/api/program/1/pipeline/1/execution/1/phase/1/step/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");

    client.verify(getStep, VerificationTimes.once());
    client.verify(get, VerificationTimes.never());
    client.clear(getStep);
    client.clear(get);
  }

  @Test
  void parseEvent_pipelineStepEnd() throws IOException, CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    // Parse
    String body = IOUtils.resourceToString("pipeline/execution/event/pipeline-step-end.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    PipelineExecutionStepEndEvent event = (PipelineExecutionStepEndEvent) executionApi.parseEvent(body);
    assertNotNull(event);

    // Step State detail fetch
    HttpRequest getStep = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(getStep).respond(response().withBody(GET_STEP_BODY));
    client.when(get).respond(response().withBody(GET_BODY));
    assertNotNull(event.getStepState());
    client.verify(getStep, VerificationTimes.once());
    client.verify(get, VerificationTimes.once());
    client.clear(getStep);
    client.clear(get);

    // Step State not found
    getStep = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1");
    client.when(getStep).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, event::getStepState, "Exception thrown.");
    assertEquals(String.format("Cannot get execution step state: %s/api/program/1/pipeline/1/execution/1/phase/1/step/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");

    client.verify(getStep, VerificationTimes.once());
    client.verify(get, VerificationTimes.never());
    client.clear(getStep);
    client.clear(get);
  }

  @Test
  void parseEvent_pipelineEnd() throws IOException, CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    String body = IOUtils.resourceToString("pipeline/execution/event/pipeline-end.json", Charset.defaultCharset(), PipelineExecutionTest.class.getClassLoader());
    PipelineExecutionEndEvent event = (PipelineExecutionEndEvent) executionApi.parseEvent(body);
    assertNotNull(event);
    assertNotNull(event.getExecution());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getStepState_via_predicate() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    com.adobe.aio.cloudmanager.PipelineExecution execution = executionApi.get("1", "1", "1");
    Optional<PipelineExecutionStepState> step = execution.getStep((s) -> StepAction.deploy == s.getStepAction());
    assertTrue(step.isEmpty());

    step = execution.getStep(PipelineExecutionStepState.IS_CURRENT);
    assertTrue(step.isPresent());
    assertEquals(StepAction.build, step.get().getStepAction());

    step = execution.getStep(PipelineExecutionStepState.IS_RUNNING);
    assertTrue(step.isPresent());
    assertEquals(StepAction.build, step.get().getStepAction());

    client.verify(get);
    client.clear(get);
  }

}

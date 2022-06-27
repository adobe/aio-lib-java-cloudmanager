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

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.UUID;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Metric;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.PipelineExecution;
import com.adobe.aio.cloudmanager.PipelineExecutionStepState;
import com.adobe.aio.cloudmanager.StepAction;
import com.adobe.aio.cloudmanager.event.PipelineExecutionEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionEndEventEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStartEventEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepStartEventEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import com.adobe.aio.cloudmanager.feign.client.PipelineExecutionApiClient;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class PipelineExecutionApiTest extends AbstractApiClientTest {

  private static final JsonBody GET_BODY = loadBodyJson("execution/get.json");
  private static final JsonBody GET_WAITING_BODY = loadBodyJson("execution/approval-waiting.json");
  private static final JsonBody GET_CODE_QUALITY_BODY = loadBodyJson("execution/codeQuality.json");

  @Test
  void getCurrent_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    assertFalse(underTest.getCurrentExecution("1", "1").isPresent());
    client.verify(get, VerificationTimes.exactly(1));

    client.clear(get);
  }

  @Test
  void getCurrent_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withBody(PipelineApiTest.LIST_BODY));
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    PipelineExecution execution = underTest.getCurrentExecution(pipeline).get();
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("1", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("1", execution.getProgramId(), "Program Id matches");

    client.clear(get);
  }

  @Test
  void start_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest put = request().withMethod("PUT").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot create execution: %s/api/program/1/pipeline/1/execution (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.clear(put);
  }

  @Test
  void start_failure412_running() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest put = request().withMethod("PUT").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(PRECONDITION_FAILED_412.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("1", "1"), "Exception thrown");
    assertEquals("Cannot create execution. Pipeline already running.", exception.getMessage(), "Message was correct");

    client.verify(put, VerificationTimes.exactly(1));
    client.clear(put);
  }

  @Test
  void start_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest put = request().withMethod("PUT").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(CREATED_201.code()).withBody(loadBodyJson("execution/start.json")));

    PipelineExecution execution = underTest.startExecution("1", "1");
    assertEquals("/api/program/1/pipeline/1/execution/5000", ((PipelineExecutionImpl) execution).getLinks().getSelf().getHref(), "URL was correct");

    client.verify(put, VerificationTimes.exactly(1));
    client.clear(put);
  }

  @Test
  void start_via_pipeline() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withPath("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(get).respond(response().withBody(PipelineApiTest.LIST_BODY));

    HttpRequest put = request().withMethod("PUT").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(CREATED_201.code()).withBody(loadBodyJson("execution/start.json")));

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    PipelineExecution execution = pipeline.startExecution();
    assertEquals("/api/program/1/pipeline/1/execution/5000", ((PipelineExecutionImpl) execution).getLinks().getSelf().getHref(), "URL was correct");

    client.clear(get);
    client.clear(put);
  }

  @Test
  void get_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution("1", "1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get execution: %s/api/program/1/pipeline/1/execution/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("1", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("1", execution.getProgramId(), "Program Id matches");

    client.clear(get);
  }

  @Test
  void get_invalidUrl() {

    PipelineExecutionStartEvent event = new PipelineExecutionStartEvent().event(
        new PipelineExecutionStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("git://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1")
        )
    );
    CloudManagerApiException e = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution(event), "Exception thrown.");
    assertEquals("Cannot get execution: unknown protocol: git.", e.getMessage(), "Message was correct.");
  }

  @Test
  void get_startEvent_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    PipelineExecutionStartEvent event = new PipelineExecutionStartEvent().event(
        new PipelineExecutionStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("https://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1")
        )
    );
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException e = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution(event), "Exception thrown.");
    assertEquals(String.format("Cannot get execution: %s/api/program/1/pipeline/1/execution/1 (404 Not Found).", baseUrl), e.getMessage(), "Message was correct.");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void get_startEvent() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    PipelineExecutionStartEvent event = new PipelineExecutionStartEvent().event(
        new PipelineExecutionStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("https://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1")
        )
    );
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecution execution = underTest.getExecution(event);
    assertEquals("1", execution.getId());
    assertEquals("1", execution.getPipelineId());
    assertEquals("1", execution.getProgramId());

    client.clear(get);
  }

  @Test
  void get_endEvent_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    PipelineExecutionEndEvent event = new PipelineExecutionEndEvent().event(
        new PipelineExecutionEndEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("https://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1")
        )
    );
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException e = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution(event), "Exception thrown.");
    assertEquals(String.format("Cannot get execution: %s/api/program/1/pipeline/1/execution/1 (404 Not Found).", baseUrl), e.getMessage(), "Message was correct.");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void get_endEvent() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    PipelineExecutionEndEvent event = new PipelineExecutionEndEvent().event(
        new PipelineExecutionEndEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("https://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1")
        )
    );
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecution execution = underTest.getExecution(event);
    assertEquals("1", execution.getId());
    assertEquals("1", execution.getPipelineId());
    assertEquals("1", execution.getProgramId());

    client.clear(get);
  }

  @Test
  void get_via_pipeline() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withBody(PipelineApiTest.LIST_BODY));
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    PipelineExecution execution = pipeline.getExecution("1");
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("1", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("1", execution.getProgramId(), "Program Id matches");

    client.verify(list, VerificationTimes.exactly(1));
    client.clear(list);
    client.clear(get);
  }

  @Test
  void isExecutionRunning_notStarted() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/not-started.json")));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    assertTrue(underTest.isExecutionRunning(execution));
    client.verify(get, VerificationTimes.exactly(2));
    client.clear(get);
  }

  @Test
  void isExecutionRunning_running() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/running.json")));

    assertTrue(underTest.isExecutionRunning("1", "1", "1"));

    client.clear(get);
  }

  @Test
  void isExecutionRunning_cancelling() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/cancelling.json")));

    assertTrue(underTest.isExecutionRunning("1", "1", "1"));

    client.clear(get);
  }

  @Test
  void isExecutionRunning_cancelled() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/cancelled.json")));

    assertFalse(underTest.isExecutionRunning("1", "1", "1"));

    client.clear(get);
  }

  @Test
  void isExecutionRunning_finished() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/finished.json")));

    assertFalse(underTest.isExecutionRunning("1", "1", "1"));

    client.clear(get);
  }

  @Test
  void isExecutionRunning_error() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/error.json")));

    assertFalse(underTest.isExecutionRunning("1", "1", "1"));

    client.clear(get);
  }

  @Test
  void isExecutionRunning_failed() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/failed.json")));

    assertFalse(underTest.isExecutionRunning("1", "1", "1"));
    client.clear(get);
  }

  @Test
  void getStepState_failure404() throws CloudManagerApiException {

    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));
    PipelineExecution execution = underTest.getCurrentExecution("1", "1").get();
    client.clear(get);
    get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepState(execution, "build"), "Exception thrown.");
    assertEquals(String.format("Cannot get execution: %s/api/program/1/pipeline/1/execution/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getStepState_failure_nostep() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/no-steps.json")));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepState(execution, "deploy"), "Exception thrown.");
    assertEquals("Cannot find step state for action 'deploy' on execution 1.", exception.getMessage(), "Message was correct");

    client.clear(get);
  }

  @Test
  void getStepState_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest getExecution = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(getExecution).respond(response().withBody(GET_BODY));

    HttpRequest getStep = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2");
    client.when(getStep).respond(response().withBody(loadBodyJson("execution/step-waiting.json")));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    assertNotNull(underTest.getExecutionStepState(execution, "codeQuality"));

    client.clear(getExecution);
    client.clear(getStep);
  }

  @Test
  void get_via_stepState_nolink() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(execution, "codeQuality");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> stepState.getExecution(), "Error thrown");
    assertEquals("Cannot find execution link for the current step (/api/program/1/pipeline/1/execution/1/phase/2/step/2).", exception.getMessage(), "Message was correct");

    client.clear(get);
  }

  @Test
  void get_via_stepState() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(execution, "build");
    client.clear(get);
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecution found = stepState.getExecution();
    assertEquals(execution.getProgramId(), found.getProgramId());
    assertEquals(execution.getPipelineId(), found.getPipelineId());
    assertEquals(execution.getId(), found.getId());

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getStepState_event_failure404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    PipelineExecutionStepStartEvent event = new PipelineExecutionStepStartEvent().event(
        new PipelineExecutionStepStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecutionStepState()._atId("https://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1/phase/1/step/1")
        )
    );
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepState(event), "Exception thrown");
    assertEquals(String.format("Cannot get step state: %s/api/program/1/pipeline/1/execution/1/phase/1/step/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getStepState_startEvent() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    PipelineExecutionStepStartEvent event = new PipelineExecutionStepStartEvent().event(
        new PipelineExecutionStepStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecutionStepState()._atId("https://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1/phase/2/step/1")
        )
    );
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/step-running.json")));

    PipelineExecutionStepState stepState = underTest.getExecutionStepState(event);
    assertEquals(PipelineExecutionStepState.Status.RUNNING, stepState.getStatusState());

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getStepState_waitingEvent() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    PipelineExecutionStepWaitingEvent event = new PipelineExecutionStepWaitingEvent().event(
        new PipelineExecutionStepStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecutionStepState()._atId("https://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1/phase/2/step/2")
        )
    );
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2");
    client.when(get).respond(response().withBody(loadBodyJson("execution/step-waiting.json")));

    PipelineExecutionStepState stepState = underTest.getExecutionStepState(event);
    assertEquals(PipelineExecutionStepState.Status.WAITING, stepState.getStatusState());

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getStepState_endEvent() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    PipelineExecutionStepEndEvent event = new PipelineExecutionStepEndEvent().event(
        new PipelineExecutionStepStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecutionStepState()._atId("https://cloudmanager.adobe.io/api/program/1/pipeline/1/execution/1/phase/2/step/1")
        )
    );
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/step-finished.json")));

    PipelineExecutionStepState stepState = underTest.getExecutionStepState(event);
    assertEquals(PipelineExecutionStepState.Status.FINISHED, stepState.getStatusState());

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getCurrentStep_failure_noEmbedded() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));
    PipelineExecution execution = underTest.getCurrentExecution("1", "1").get();
    client.clear(get);
    get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/no-embedded.json")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getCurrentStep(execution), "Exception thrown.");
    assertEquals("Cannot find a current step for pipeline 1.", exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getCurrentStep_failure_nostep() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(loadBodyJson("execution/no-steps.json")));
    PipelineExecution execution = underTest.getCurrentExecution("1", "1").get();
    client.clear(get);
    get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/no-steps.json")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getCurrentStep(execution), "Exception thrown.");
    assertEquals("Cannot find a current step for pipeline 1.", exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getCurrentStep_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    client.clear(get);

    get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecutionStepState stepState = underTest.getCurrentStep(execution);
    assertEquals(StepAction.build.toString(), stepState.getAction(), "Correct step action.");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void getWaitingStep_failure_noactive() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    client.clear(get);
    get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/no-active.json")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getWaitingStep(execution), "Exception thrown.");
    assertEquals("Cannot find a waiting step for pipeline 1.", exception.getMessage(), "Message was correct");

    client.clear(get);
  }

  @Test
  void getWaitingStep_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    PipelineExecutionStepState stepState = underTest.getWaitingStep(execution);
    assertEquals("approval", stepState.getAction(), "Correct step action.");
    client.verify(get, VerificationTimes.exactly(2));
    client.clear(get);
  }

  @Test
  void advanceCurrent_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    HttpRequest put = request().withMethod("PUT").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/.*");

    underTest.advanceCurrentExecution("1", "1");
    client.verify(get, VerificationTimes.exactly(1));
    client.verify(put, VerificationTimes.exactly(0));
    client.clear(get);
  }

  @Test
  void advanceCurrent_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/3/step/4/advance")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"approved\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));
    underTest.advanceCurrentExecution("1", "1");
    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void advance_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/3/step/4/advance")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"approved\": true }"));
    client.when(put).respond(response().withStatusCode(NOT_FOUND_404.code()));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.advanceExecution(execution), "Exception thrown");
    assertEquals(String.format("Cannot advance execution: %s/api/program/1/pipeline/1/execution/1/phase/3/step/4/advance (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");

    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void advance_buildRunning() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.advanceExecution("1", "1", "1"), "Exception thrown");
    assertEquals("Cannot find a waiting step for pipeline 1.", exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void advance_codeQualityWaiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest metrics = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(metrics).respond(response().withBody(loadBodyJson("execution/metrics.json")));

    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/advance")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(loadBodyJson("execution/put-metrics-override.json"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    underTest.advanceExecution(execution);

    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(metrics);
    client.clear(put);
  }

  @Test
  void advance_codeQualityWaiting_via_execution() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest metrics = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(metrics).respond(response().withBody(loadBodyJson("execution/metrics.json")));

    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/advance")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(loadBodyJson("execution/put-metrics-override.json"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    execution.advance();

    client.verify(get, VerificationTimes.exactly(2));
    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(metrics);
    client.clear(put);
  }

  @Test
  void advance_approvalWaiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/3/step/4/advance")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"approved\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    underTest.advanceExecution("1", "1", "1");

    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void advance_approvalWaiting_via_execution() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/3/step/4/advance")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"approved\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    execution.advance();

    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancelCurrent_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    HttpRequest put = request().withMethod("PUT").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/.*");
    underTest.cancelCurrentExecution("1", "1");
    client.verify(get, VerificationTimes.exactly(1));
    client.verify(put, VerificationTimes.exactly(0));
    client.clear(get);
  }

  @Test
  void cancelCurrent_buildRunning() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest put = request().withBody("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"cancel\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    underTest.cancelCurrentExecution("1", "1");
    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_failure403() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest put = request().withBody("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"cancel\": true }"));
    client.when(put).respond(response().withStatusCode(FORBIDDEN_403.code()));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution(execution), "Exception Thrown");
    assertEquals(String.format("Cannot cancel execution: %s/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest put = request().withBody("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"cancel\": true }"));
    client.when(put).respond(response().withStatusCode(NOT_FOUND_404.code()));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution(execution), "Exception Thrown");
    assertEquals(String.format("Cannot cancel execution: %s/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_buildRunning() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest put = request().withBody("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"cancel\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    underTest.cancelExecution(execution);
    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_via_execution() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest put = request().withBody("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"cancel\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    execution.cancel();
    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_codeQualityWaiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest put = request().withBody("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/cancel")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"override\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    underTest.cancelExecution("1", "1", "1");
    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_codeQualityWaiting_errorState() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/running.json")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution("1", "1", "1"), "Exception thrown");
    assertEquals("Cannot find a cancel link for the current step (build). Step may not be cancellable.", exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.exactly(1));
    client.clear(get);
  }

  @Test
  void cancel_approvalWaiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));
    HttpRequest put = request().withBody("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/3/step/4/cancel")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"approved\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));
    underTest.cancelExecution("1", "1", "1");
    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_deployWaiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/deploy-waiting.json")));
    HttpRequest put = request().withBody("PUT")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/2/advance")
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(json("{ \"resume\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));
    underTest.cancelExecution("1", "1", "1");
    client.verify(put, VerificationTimes.exactly(1));
    client.clear(get);
    client.clear(put);
  }

  @Test
  void getExecutionStepLogDownloadUrl_nolink() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl(execution, "validate"), "Exception Thrown");
    assertEquals("Could not find logs link for action 'validate'.", exception.getMessage(), "Message was correct.");
    client.clear(get);
  }

  @Test
  void getExecutionStepLogDownloadUrl_nullRedirect() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest redirect = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/logs");
    client.when(redirect).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl("1", "1", "1", "codeQuality"), "Exception thrown");
    assertEquals("Log [/api/program/1/pipeline/1/execution/1/phase/2/step/2/logs] did not contain a redirect. Was: null.", exception.getMessage(), "Message was correct.");
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getExecutionStepLogDownloadUrl_failure403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest redirect = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(redirect).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl("1", "1", "1", "build"), "Exception thrown");
    assertEquals(String.format("Cannot get logs: %s/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getExecutionStepLogDownloadUrl_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest redirect = setupDownloadUrl(sessionId);

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    assertEquals(String.format("%s/logs/special.txt", baseUrl), underTest.getExecutionStepLogDownloadUrl(execution, "build"));
    client.clear(get);
    client.clear(redirect);
  }

  private HttpRequest setupDownloadUrl(String sessionId) {
    HttpRequest redirect = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(redirect).respond(
        response()
            .withStatusCode(OK_200.code())
            .withBody(String.format("{ \"redirect\": \"%s/logs/special.txt\" }", baseUrl))
    );
    return redirect;
  }

  @Test
  void getExecutionStepLogDownloadUrl_success_alternateFile() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest redirect = setupDownloadUrlSpecial(sessionId);

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    assertEquals(String.format("%s/logs/somethingspecial.txt", baseUrl), underTest.getExecutionStepLogDownloadUrl(execution, "build", "somethingspecial"));
    client.clear(get);
    client.clear(redirect);
  }

  private HttpRequest setupDownloadUrlSpecial(String sessionId) {
    HttpRequest redirect = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
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
  void downloadStepLog_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest redirect = setupDownloadUrl(sessionId);
    HttpRequest download = setupFileContent("special");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    underTest.downloadExecutionStepLog(execution, "build", bos);
    assertEquals("some log line\nsome other log line\n", bos.toString());
    client.clear(get);
    client.clear(redirect);
    client.clear(download);
  }

  private HttpRequest setupFileContent(String filename) {
    HttpRequest download = request().withMethod("GET").withPath(String.format("/logs/%s.txt", filename));
    client.when(download).respond(
        response()
            .withStatusCode(OK_200.code())
            .withBody("some log line\nsome other log line\n")
    );
    return download;
  }

  @Test
  void downloadStepLog_success_alternateFile() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest redirect = setupDownloadUrlSpecial(sessionId);
    HttpRequest download = setupFileContent("somethingspecial");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    underTest.downloadExecutionStepLog(execution, "build", "somethingspecial", bos);
    assertEquals("some log line\nsome other log line\n", bos.toString());
    client.clear(get);
    client.clear(redirect);
    client.clear(download);
  }

  @Test
  void downloadStepLog_via_stepState() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest redirect = setupDownloadUrl(sessionId);
    HttpRequest download = setupFileContent("special");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(execution, "build");
    stepState.getLog(bos);
    assertEquals("some log line\nsome other log line\n", bos.toString());
    client.clear(get);
    client.clear(redirect);
    client.clear(download);
  }

  @Test
  void downloadStepLog_via_stepState_named() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest redirect = setupDownloadUrlSpecial(sessionId);
    HttpRequest download = setupFileContent("somethingspecial");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(execution, "build");
    stepState.getLog("somethingspecial", bos);
    assertEquals("some log line\nsome other log line\n", bos.toString());
    client.clear(get);
    client.clear(redirect);
    client.clear(download);
  }

  @Test
  void getStepMetrics_noLink() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getQualityGateResults(execution, "codeQuality"), "Exception thrown");
    assertEquals(String.format("Could not find metric link for action (codeQuality) on pipeline 1", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(get);
  }

  @Test
  void getStepMetrics_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));
    HttpRequest metrics = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(metrics).respond(response().withStatusCode(NOT_FOUND_404.code()));
    PipelineExecution execution = underTest.getExecution("1", "1", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getQualityGateResults(execution, "codeQuality"), "Exception thrown");
    assertEquals(String.format("Cannot get metrics: %s/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.clear(get);
  }

  @Test
  void listExecutions_failure404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest listPipelines = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(listPipelines).respond(response().withBody(loadBodyJson("pipeline/list.json")));

    HttpRequest list = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listExecutions(pipeline), "Exception thrown");
    assertEquals(String.format("Cannot list executions: %s/api/program/1/pipeline/1/executions (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list, VerificationTimes.exactly(1));
    client.clear(listPipelines);
    client.clear(list);
  }

  @Test
  void listExecutions_success_emptyList() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest listPipelines = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(listPipelines).respond(response().withBody(loadBodyJson("pipeline/list.json")));

    HttpRequest empty = request()
        .withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/executions");
    client.when(empty).respond(response().withBody(loadBodyJson("execution/list-empty.json")));

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    Iterator<PipelineExecution> iterator = underTest.listExecutions(pipeline, 30);
    assertFalse(iterator.hasNext(), "Iterator Empty.");
    client.verify(empty, VerificationTimes.exactly(1));
    client.clear(listPipelines);
    client.clear(empty);
  }

  @Test
  void listExecutions_success_onePage() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest listPipelines = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(listPipelines).respond(response().withBody(loadBodyJson("pipeline/list.json")));

    HttpRequest empty = request().withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter(PipelineExecutionApiClient.START_PARAM, "20")
        .withQueryStringParameter(PipelineExecutionApiClient.LIMIT_PARAM, "20");
    client.when(empty).respond(response().withBody(loadBodyJson("execution/list-empty.json")));

    HttpRequest start = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(start).respond(response().withBody(loadBodyJson("execution/list-start.json")));

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    Iterator<PipelineExecution> iterator = underTest.listExecutions(pipeline);
    int counter = 0;
    while (iterator.hasNext()) {
      counter++;
      iterator.next();
    }
    assertEquals(20, counter, "Correct list length.");

    client.clear(listPipelines);
    client.clear(start);
    client.clear(empty);
  }

  @Test
  void listExecutions_success_multiPage() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest listPipelines = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipelines");
    client.when(listPipelines).respond(response().withBody(loadBodyJson("pipeline/list.json")));

    HttpRequest middle = request().withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter(PipelineExecutionApiClient.START_PARAM, "20")
        .withQueryStringParameter(PipelineExecutionApiClient.LIMIT_PARAM, "20");
    client.when(middle).respond(response().withBody(loadBodyJson("execution/list-middle.json")));

    HttpRequest end = request().withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter(PipelineExecutionApiClient.START_PARAM, "40")
        .withQueryStringParameter(PipelineExecutionApiClient.LIMIT_PARAM, "20");
    client.when(end).respond(response().withBody(loadBodyJson("execution/list-end.json")));

    HttpRequest empty = request().withMethod("GET")
        .withHeader("x-api-key", sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter(PipelineExecutionApiClient.START_PARAM, "60")
        .withQueryStringParameter(PipelineExecutionApiClient.LIMIT_PARAM, "20");
    client.when(empty).respond(response().withBody(loadBodyJson("execution/list-empty.json")));

    HttpRequest start = request().withMethod("GET").withHeader("x-api-key", sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(start).respond(response().withBody(loadBodyJson("execution/list-start.json")));

    Pipeline pipeline = underTest.listPipelines("1", new Pipeline.IdPredicate("1")).stream().findFirst().get();
    Iterator<PipelineExecution> iterator = underTest.listExecutions(pipeline);
    int counter = 0;
    while (iterator.hasNext()) {
      counter++;
      iterator.next();
    }
    assertEquals(45, counter, "Correct list length.");
    client.verify(empty, VerificationTimes.exactly(1));
    client.clear(listPipelines);
    client.clear(start);
    client.clear(middle);
    client.clear(end);
    client.clear(empty);
  }

  @Test
  void Pipeline_Status() {
    assertEquals(PipelineExecution.Status.fromValue("FAILED"), PipelineExecution.Status.FAILED);
    assertNull(PipelineExecution.Status.fromValue("foo"));
    assertEquals(PipelineExecution.Status.FAILED.getValue(), PipelineExecution.Status.FAILED.toString());
  }

  @Test
  void PipelineStepState_Status() {
    assertEquals(PipelineExecutionStepState.Status.fromValue("FAILED"), PipelineExecutionStepState.Status.FAILED);
    assertNull(PipelineExecutionStepState.Status.fromValue("foo"));
    assertEquals(PipelineExecutionStepState.Status.FAILED.getValue(), PipelineExecutionStepState.Status.FAILED.toString());
  }

  @Test
  void Metric_Sev() {
    assertEquals(Metric.Severity.fromValue("informational"), Metric.Severity.INFORMATIONAL);
    assertNull(Metric.Severity.fromValue("foo"));
    assertEquals(Metric.Severity.INFORMATIONAL.getValue(), Metric.Severity.INFORMATIONAL.toString());
  }

  @Test
  void Metric_Comp() {
    assertEquals(Metric.Comparator.fromValue("NEQ"), Metric.Comparator.NEQ);
    assertNull(Metric.Comparator.fromValue("foo"));
    assertEquals(Metric.Comparator.NEQ.getValue(), Metric.Comparator.NEQ.toString());
  }
}

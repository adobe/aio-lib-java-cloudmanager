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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Metric;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.PipelineExecution;
import com.adobe.aio.cloudmanager.PipelineExecutionStepState;
import com.adobe.aio.cloudmanager.event.PipelineExecutionEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionEndEventEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStartEventEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepStartEventEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.model.ClearType;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;

public class PipelineExecutionsTest extends AbstractApiClientTest {
  public static List<String> getTestExpectationFiles() {
    return Arrays.asList(
        "executions/pipelines.json",
        "executions/current-success.json",
        "executions/start-execution-notfound.json",
        "executions/start-execution-running.json",
        "executions/start-execution-success.json",
        "executions/get-successes.json",
        "executions/is-running-checks.json",
        "executions/step-state.json",
        "executions/current-no-steps.json",
        "executions/current-no-active-step.json",
        "executions/specific-not-found.json",
        "executions/specific-code-quality.json",
        "executions/specific-approval-waiting.json",
        "executions/specific-advance-running.json",
        "executions/specific-cancel.json",
        "executions/specific-cancel-codeQuality-invalid.json",
        "executions/specific-cancel-deploy-waiting.json",
        "executions/specific-cancel-deploy-invalid.json"
    );
  }

  @BeforeEach
  public void setupLogsForExecutions() {
    
    client.when(
        request().withMethod("GET").withPath("/logs/special.txt")
    ).respond(
        HttpResponse.response()
            .withStatusCode(HttpStatusCode.OK_200.code())
            .withBody("some log line\nsome other log line\n")
    );

    client.when(
        request().withMethod("GET").withPath("/logs/somethingspecial.txt")
    ).respond(
        HttpResponse.response()
            .withStatusCode(HttpStatusCode.OK_200.code())
            .withBody("some log line\nsome other log line\n")
    );
  }

  @Test
  void getCurrentExecution_failure404() throws CloudManagerApiException {
    assertFalse(underTest.getCurrentExecution("4", "1").isPresent());
  }

  @Test
  void getCurrentExecution_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getCurrentExecution("4", "2").get();
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("2", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("4", execution.getProgramId(), "Program Id matches");
  }

  @Test
  void startExecution_failure404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("3", "3"), "Exception thrown");
    assertEquals(String.format("Cannot create execution: %s/api/program/3/pipeline/3/execution (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_failure412_running() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("3", "2"), "Exception thrown");
    assertEquals("Cannot create execution. Pipeline already running.", exception.getMessage(), "Message was correct");
  }

  @Test
  void startExecution_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.startExecution("3", "1");
    assertEquals("/api/program/3/pipeline/1/execution/5000", ((PipelineExecutionImpl) execution).getLinks().getSelf().getHref(), "URL was correct");
  }

  @Test
  void startExecution_via_pipeline() throws Exception {
    Collection<Pipeline> pipelines = underTest.listPipelines("3");
    Pipeline pipeline = pipelines.stream().filter(p -> p.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    PipelineExecution execution = pipeline.startExecution();
    assertEquals("/api/program/3/pipeline/1/execution/5000", ((PipelineExecutionImpl) execution).getLinks().getSelf().getHref(), "URL was correct");
  }

  @Test
  void getExecution_failure404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution("4", "3", "10"), "Exception thrown for 404");
    assertEquals(String.format("Cannot get execution: %s/api/program/4/pipeline/3/execution/10 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecution_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("3", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("4", execution.getProgramId(), "Program Id matches");
  }

  @Test
  void getExecution_startEvent_failure404() {
    PipelineExecutionStartEvent event = new PipelineExecutionStartEvent().event(
        new PipelineExecutionStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("https://cloudmanager.adobe.io/api/program/4/pipeline/4/execution/10")
        )
    );
    CloudManagerApiException e = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution(event), "Exception thrown for 404");
    assertEquals(String.format("Cannot get execution: %s/api/program/4/pipeline/4/execution/10 (404 Not Found).", baseUrl), e.getMessage(), "Message was correct.");
  }

  @Test
  void getExecution_startEvent() throws CloudManagerApiException {
    PipelineExecutionStartEvent event = new PipelineExecutionStartEvent().event(
        new PipelineExecutionStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("https://cloudmanager.adobe.io/api/program/4/pipeline/4/execution/1")
        )
    );
    PipelineExecution execution = underTest.getExecution(event);
    assertEquals("1", execution.getId());
    assertEquals("4", execution.getPipelineId());
    assertEquals("4", execution.getProgramId());
  }

  @Test
  void getExecution_endEvent_failure404() {
    PipelineExecutionEndEvent event = new PipelineExecutionEndEvent().event(
        new PipelineExecutionEndEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("https://cloudmanager.adobe.io/api/program/4/pipeline/4/execution/10")
        )
    );
    CloudManagerApiException e = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution(event), "Exception thrown for 404");
    assertEquals(String.format("Cannot get execution: %s/api/program/4/pipeline/4/execution/10 (404 Not Found).", baseUrl), e.getMessage(), "Message was correct.");
  }

  @Test
  void getExecution_endEvent() throws CloudManagerApiException {
    PipelineExecutionEndEvent event = new PipelineExecutionEndEvent().event(
        new PipelineExecutionEndEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecution()._atId("https://cloudmanager.adobe.io/api/program/4/pipeline/4/execution/1")
        )
    );
    PipelineExecution execution = underTest.getExecution(event);
    assertEquals("1", execution.getId());
    assertEquals("4", execution.getPipelineId());
    assertEquals("4", execution.getProgramId());
  }

  @Test
  void getExecution_via_pipeline() throws CloudManagerApiException {
    Pipeline pipeline = underTest.listPipelines("4", p -> p.getId().equals("3")).stream().findFirst().orElse(null);
    PipelineExecution execution = pipeline.getExecution("1");
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("3", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("4", execution.getProgramId(), "Program Id matches");
  }

  @Test
  void isExecutionRunning_notStarted() throws CloudManagerApiException {
    assertTrue(underTest.isExecutionRunning("4", "7", "10"));
  }

  @Test
  void isExecutionRunning_running() throws CloudManagerApiException {
    assertTrue(underTest.isExecutionRunning("4", "7", "11"));
  }

  @Test
  void isExecutionRunning_cancelling() throws CloudManagerApiException {
    assertTrue(underTest.isExecutionRunning("4", "7", "12"));
  }

  @Test
  void isExecutionRunning_canceled() throws CloudManagerApiException {
    assertFalse(underTest.isExecutionRunning("4", "7", "13"));
  }

  @Test
  void isExecutionRunning_finished() throws CloudManagerApiException {
    assertFalse(underTest.isExecutionRunning("4", "7", "14"));
  }

  @Test
  void isExecutionRunning_error() throws CloudManagerApiException {
    assertFalse(underTest.isExecutionRunning("4", "7", "15"));
  }

  @Test
  void isExecutionRunning_failed() throws CloudManagerApiException {
    assertFalse(underTest.isExecutionRunning("4", "7", "16"));
  }

  @Test
  void getExecutionStepState_failure404() {

    PipelineExecution execution = new PipelineExecution() {
      @Override
      public String getId() {
        return "10";
      }

      @Override
      public String getProgramId() {
        return "4";
      }

      @Override
      public String getPipelineId() {
        return "3";
      }

      @Override
      public Status getStatusState() {
        return null;
      }

      @Override
      public void advance() throws CloudManagerApiException {

      }

      @Override
      public void cancel() throws CloudManagerApiException {

      }
    };
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepState(execution, "build"), "Exception thrown for 404");
    assertEquals(String.format("Cannot get execution: %s/api/program/4/pipeline/3/execution/10 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecutionStepState_failure_nostep() throws CloudManagerApiException {

    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepState(execution, "deploy"), "Exception thrown for missing step");
    assertEquals("Cannot find step state for action deploy on execution 1.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecutionStepState_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    assertNotNull(underTest.getExecutionStepState(execution, "codeQuality"));
  }

  @Test
  void getExecution_via_stepState_nolink() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(execution, "codeQuality");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> stepState.getExecution(), "Error thrown");
    assertEquals("Cannot find execution link for the current step (/api/program/4/pipeline/3/execution/1/phase/4596/step/8493).", exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecution_via_stepState() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(execution, "build");
    PipelineExecution found = stepState.getExecution();
    assertEquals(execution.getProgramId(), found.getProgramId());
    assertEquals(execution.getPipelineId(), found.getPipelineId());
    assertEquals(execution.getId(), found.getId());
  }

  @Test
  void getExecutionStepState_event_failure404() {
    PipelineExecutionStepStartEvent event = new PipelineExecutionStepStartEvent().event(
        new PipelineExecutionStepStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecutionStepState()._atId("https://cloudmanager.adobe.io/api/program/4/pipeline/10/execution/1/phase/4596/step/8491")
        )
    );
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepState(event), "Exception thrown");
    assertEquals(String.format("Cannot get step state: %s/api/program/4/pipeline/10/execution/1/phase/4596/step/8491 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecutionStepState_startEvent() throws CloudManagerApiException {
    PipelineExecutionStepStartEvent event = new PipelineExecutionStepStartEvent().event(
        new PipelineExecutionStepStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecutionStepState()._atId("https://cloudmanager.adobe.io/api/program/4/pipeline/3/execution/4/phase/4596/step/8491")
        )
    );
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(event);
    assertEquals(PipelineExecutionStepState.Status.RUNNING, stepState.getStatusState());
  }

  @Test
  void getExecutionStepState_waitingEvent() throws CloudManagerApiException {
    PipelineExecutionStepWaitingEvent event = new PipelineExecutionStepWaitingEvent().event(
        new PipelineExecutionStepStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecutionStepState()._atId("https://cloudmanager.adobe.io/api/program/4/pipeline/3/execution/4/phase/4596/step/8492")
        )
    );
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(event);
    assertEquals(PipelineExecutionStepState.Status.WAITING, stepState.getStatusState());
  }

  @Test
  void getExecutionStepState_endEvent() throws CloudManagerApiException {
    PipelineExecutionStepEndEvent event = new PipelineExecutionStepEndEvent().event(
        new PipelineExecutionStepStartEventEvent().activitystreamsobject(
            new com.adobe.aio.cloudmanager.event.PipelineExecutionStepState()._atId("https://cloudmanager.adobe.io/api/program/4/pipeline/3/execution/4/phase/4596/step/8493")
        )
    );
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(event);
    assertEquals(PipelineExecutionStepState.Status.FINISHED, stepState.getStatusState());
  }

  @Test
  void getCurrentStep_failure_nostep() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getCurrentExecution("4", "4").get();
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getCurrentStep(execution), "Exception thrown.");
    assertEquals("Cannot find a current step for pipeline 4.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getCurrentStep_failure_noactive() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getCurrentExecution("4", "5").get();
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getCurrentStep(execution), "Exception thrown.");
    assertEquals("Cannot find a current step for pipeline 5.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getCurrentStep_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    PipelineExecutionStepState stepState = underTest.getCurrentStep(execution);
    assertEquals("build", stepState.getAction(), "Correct step action.");
  }

  @Test
  void getWaitingStep_failure_nostep() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getCurrentExecution("4", "4").get();
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getWaitingStep(execution), "Exception thrown.");
    assertEquals("Cannot find a waiting step for pipeline 4.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getWaitingStep_failure_noactive() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getCurrentExecution("4", "5").get();
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getWaitingStep(execution), "Exception thrown.");
    assertEquals("Cannot find a waiting step for pipeline 5.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getWaitingStep_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    PipelineExecutionStepState stepState = underTest.getWaitingStep(execution);
    assertEquals("approval", stepState.getAction(), "Correct step action.");
  }

  @Test
  void advanceCurrentExecution_failure404() throws CloudManagerApiException {
    underTest.advanceCurrentExecution("4", "10");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/10/execution/.*"), VerificationTimes.exactly(0));
    client.clear(request().withPath("/api/program/4/pipeline/10/execution/.*"), ClearType.LOG);
  }

  @Test
  void advanceCurrentExecution_success() throws CloudManagerApiException {
    underTest.advanceCurrentExecution("4", "2");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/2/execution/1/phase/4596/step/15490/advance").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/2/execution/1/phase/4596/step/15490/advance"), ClearType.LOG);
  }

  @Test
  void advanceExecution_failure404() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "7", "9");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.advanceExecution(execution), "Exception thrown");
    assertEquals(String.format("Cannot advance execution: %s/api/program/4/pipeline/7/execution/9/phase/8567/step/15490/advance (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void advanceExecution_buildRunning() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.advanceExecution("4", "3", "7"), "Exception thrown");
    assertEquals("Cannot find a waiting step for pipeline 3.", exception.getMessage(), "Message was correct");
  }

  @Test
  void advanceExecution_codeQualityWaiting() throws CloudManagerApiException {
    underTest.advanceExecution("4", "3", "2");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/advance").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/advance"), ClearType.LOG);
  }

  @Test
  void advanceExecution_codeQualityWaiting_via_execution() throws Exception {
    PipelineExecution execution = underTest.getExecution("4", "3", "2");
    execution.advance();
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/advance").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/advance"), ClearType.LOG);
  }

  @Test
  void advanceExecution_approvalWaiting() throws CloudManagerApiException {
    underTest.advanceExecution("4", "3", "4");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/4/phase/8567/step/15490/advance").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/4/phase/8567/step/15490/advance"), ClearType.LOG);
  }

  @Test
  void advanceExecution_approvalWaiting_via_execution() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    execution.advance();
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/4/phase/8567/step/15490/advance").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/4/phase/8567/step/15490/advance"), ClearType.LOG);
  }

  @Test
  void cancelCurrentExecution_failure404() throws CloudManagerApiException {
    underTest.cancelCurrentExecution("4", "10");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/10/execution/.*"), VerificationTimes.exactly(0));
    client.clear(request().withPath("/api/program/4/pipeline/10/execution/.*"), ClearType.LOG);
  }

  @Test
  void cancelCurrentExecution_buildRunning() throws CloudManagerApiException {
    underTest.cancelCurrentExecution("4", "2");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/2/execution/1/phase/4596/step/15490/cancel").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/2/execution/1/phase/4596/step/15490/cancel"), ClearType.LOG);
  }

  @Test
  void cancelExecution_failure403() throws CloudManagerApiException {
    when(workspace.getImsOrgId()).thenReturn("forbidden");

    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution(execution), "Exception Thrown");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel"), ClearType.LOG);
    assertEquals(String.format("Cannot cancel execution: %s/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void cancelExecution_failure404() throws CloudManagerApiException {
    when(workspace.getImsOrgId()).thenReturn("not-found");

    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution(execution), "Exception Thrown");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel"), ClearType.LOG);
    assertEquals(String.format("Cannot cancel execution: %s/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void cancelExecution_buildRunning() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    underTest.cancelExecution(execution);
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel"), ClearType.LOG);
  }

  @Test
  void cancelExecution_via_execution() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    execution.cancel();
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel"), ClearType.LOG);
  }

  @Test
  void cancelExecution_codeQualityWaiting() throws CloudManagerApiException {
    underTest.cancelExecution("4", "3", "2");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/cancel").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/cancel"), ClearType.LOG);
  }

  @Test
  void cancelExecution_codeQualityWaiting_errorState() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution("4", "3", "3"), "Exception thrown");
    assertEquals("Cannot find a cancel link for the current step (codeQuality). Step may not be cancellable.", exception.getMessage(), "Message was correct");
  }

  @Test
  void cancelExecution_approvalWaiting() throws CloudManagerApiException {
    underTest.cancelExecution("4", "3", "4");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/4/phase/8567/step/15490/cancel").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/4/phase/8567/step/15490/cancel"), ClearType.LOG);
  }

  @Test
  void cancelExecution_deployWaiting() throws CloudManagerApiException {
    underTest.cancelExecution("4", "3", "5");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/5/phase/8567/step/15492/advance").withContentType(MediaType.APPLICATION_JSON));
    client.clear(request().withPath("/api/program/4/pipeline/3/execution/5/phase/8567/step/15492/advance"), ClearType.LOG);
  }

  @Test
  void cancelExecution_deployWaiting_errorState() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution("4", "3", "6"), "Exception thrown");
    assertEquals("Cannot find a cancel link for the current step (deploy). Step may not be cancellable.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecutionStepLogDownloadUrl_nolink() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl(execution, "validate"), "Exception Thrown");
    assertEquals("Could not find logs link for action 'validate'.", exception.getMessage(), "Message was correct.");    
  }
  
  @Test
  void getExecutionStepLogDownloadUrl_nullRedirect() {
    client.when(
        request().withMethod("GET")
            .withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/logs")
    ).respond(
        HttpResponse.response()
            .withStatusCode(HttpStatusCode.OK_200.code())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .withBody(String.format("{}", baseUrl))
    );

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl("4", "3", "2", "codeQuality"), "Exception thrown");
    assertEquals("Log [/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/logs] did not contain a redirect. Was: null.", exception.getMessage(), "Message was correct.");
    client.clear(request().withMethod("GET")
        .withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/logs"), ClearType.ALL);
  }
  
  @Test
  void getExecutionStepLogDownloadUrl_failure403() {
    client.when(
        request().withMethod("GET")
            .withPath("/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs")
    ).respond(
        HttpResponse.response()
            .withStatusCode(HttpStatusCode.FORBIDDEN_403.code())
    );
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl("4", "3", "4", "build"), "Exception Thrown");
    client.clear(request().withMethod("GET").withPath("/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs"), ClearType.ALL);
    assertEquals(String.format("Cannot get logs: %s/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
  }
  
  @Test
  void getExecutionStepLogDownloadUrl_failure404() {
    client.when(
        request().withMethod("GET")
            .withPath("/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs")
    ).respond(
        HttpResponse.response()
            .withStatusCode(HttpStatusCode.NOT_FOUND_404.code())
    );
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl("4", "3", "4", "build"), "Exception Thrown");
    client.clear(request().withMethod("GET").withPath("/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs"), ClearType.ALL);
    assertEquals(String.format("Cannot get logs: %s/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
  }
  
  @Test
  void getExecutionStepLogDownloadUrl_success() throws CloudManagerApiException {
    setupDownloadUrl();
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    assertEquals(String.format("%s/logs/special.txt", baseUrl), underTest.getExecutionStepLogDownloadUrl(execution, "build"));
    clearDownloadUrl();
  }

  @Test
  void getExecutionStepLogDownloadUrl_success_alternateFile() throws CloudManagerApiException {
    setupDownloadUrlSpecial();
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    assertEquals(String.format("%s/logs/somethingspecial.txt", baseUrl), underTest.getExecutionStepLogDownloadUrl(execution, "build", "somethingspecial"));
    clearDownloadUrlSpecial();
  }
  
  @Test
  void downloadExecutionStepLog_success() throws CloudManagerApiException {
    setupDownloadUrl();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    underTest.downloadExecutionStepLog(execution, "build", bos);
    assertEquals("some log line\nsome other log line\n", bos.toString());
    clearDownloadUrl();
  }

  @Test
  void downloadExecutionStepLog_success_alternateFile() throws CloudManagerApiException {
    setupDownloadUrlSpecial();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    underTest.downloadExecutionStepLog(execution, "build", "somethingspecial", bos);
    assertEquals("some log line\nsome other log line\n", bos.toString());
    clearDownloadUrlSpecial();
  }


  @Test
  void downloadExecutionStepLog_via_stepState() throws CloudManagerApiException {
    setupDownloadUrl();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(execution, "build");
    stepState.getLog(bos);
    assertEquals("some log line\nsome other log line\n", bos.toString());
    clearDownloadUrl();
  }
  @Test
  void downloadExecutionStepLog_via_stepState_named() throws CloudManagerApiException {
    setupDownloadUrlSpecial();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PipelineExecution execution = underTest.getExecution("4", "3", "4");
    PipelineExecutionStepState stepState = underTest.getExecutionStepState(execution, "build");
    stepState.getLog("somethingspecial", bos);
    assertEquals("some log line\nsome other log line\n", bos.toString());
    clearDownloadUrlSpecial();
  }
  
  @Test
  void getStepMetrics_failure404() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "7", "9");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getQualityGateResults(execution, "codeQuality"), "Exception thrown");
    assertEquals(String.format("Cannot get metrics: %s/api/program/4/pipeline/7/execution/9/phase/8565/step/15484/metrics (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }


  @Test
  void PipelineExecution_Status() {
    assertEquals(PipelineExecution.Status.fromValue("FAILED"), PipelineExecution.Status.FAILED);
    assertNull(PipelineExecution.Status.fromValue("foo"));
    assertEquals(PipelineExecution.Status.FAILED.getValue(), PipelineExecution.Status.FAILED.toString());
  }

  @Test
  void PipelineExecutionStepState_Status() {
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


  private void setupDownloadUrl() {
    client.when(
        request().withMethod("GET")
            .withPath("/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs")
    ).respond(
        HttpResponse.response()
            .withStatusCode(HttpStatusCode.OK_200.code())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .withBody(String.format("{ \"redirect\": \"%s/logs/special.txt\" }", baseUrl))
    );
  }
  private void clearDownloadUrl() {
    client.clear(request().withMethod("GET")
        .withPath("/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs"), ClearType.ALL);
  }

  private void setupDownloadUrlSpecial() {
    client.when(
        request()
            .withMethod("GET")
            .withPath("/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs")
            .withQueryStringParameter("file", "somethingspecial")
    ).respond(
        HttpResponse.response()
            .withStatusCode(HttpStatusCode.OK_200.code())
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .withBody(String.format("{ \"redirect\": \"%s/logs/somethingspecial.txt\" }", baseUrl))
    );
  }
  private void clearDownloadUrlSpecial() {
    client.clear(request()
        .withMethod("GET")
        .withPath("/api/program/4/pipeline/3/execution/4/phase/4596/step/8491/logs")
        .withQueryStringParameter("file", "somethingspecial"), ClearType.ALL);
  }
}

package io.adobe.cloudmanager.impl;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 Adobe Inc.
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

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.model.Pipeline;
import io.adobe.cloudmanager.model.PipelineExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.*;

@ExtendWith(MockServerExtension.class)
public class ExecutionsTest extends AbstractApiTest {

  public static String[] getTestExpectationFiles() {
    return new String[] {
        "executions/program.json",
        "executions/pipelines.json",
        "executions/current-success.json",
        "executions/specific-success.json",
        "executions/current-no-steps.json",
        "executions/current-no-active-step.json",
        "executions/specific-code-quality.json",
        "executions/specific-code-quality-invalid.json",
        "executions/specific-approval-waiting.json",
        "executions/specific-cancel-deploy-waiting.json",
        "executions/specific-cancel-deploy-invalid.json",
        "executions/specific-advance-build-running.json"
    };
  }

  @Test
  void getCurrentExecution_executionReturns404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getCurrentExecution("4", "1"), "Exception thrown for 404");
    assertEquals(String.format("Cannot get execution: %s/api/program/4/pipeline/1/execution (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getCurrentExecution_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getCurrentExecution("4", "2");
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("2", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("4", execution.getProgramId(), "Program Id matches");
  }

  @Test
  void cancelCurrentExecution_missingPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelCurrentExecution("4", "10"), "Exception thrown");
    assertEquals("Pipeline 10 does not exist in program 4.", exception.getMessage(), "Message was correct");
  }

  @Test
  void cancelCurrentExecution_noStateSteps() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelCurrentExecution("4", "4"), "Exception thrown");
    assertEquals("Cannot find a current step for pipeline 4.", exception.getMessage(), "Message was correct");
  }

  @Test
  void cancelCurrentExecution_noActiveStateStep() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelCurrentExecution("4", "5"), "Exception thrown");
    assertEquals("Cannot find a current step for pipeline 5.", exception.getMessage(), "Message was correct");
  }

  @Test
  void cancelCurrentExecution_buildRunning() throws CloudManagerApiException {
    underTest.cancelCurrentExecution("4", "2");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/2/execution/1/phase/4596/step/8492/cancel").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void getExecution_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("4", "3", "1");
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("3", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("4", execution.getProgramId(), "Program Id matches");
  }

  @Test
  void getExecution_missingPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution("4", "10", "1"), "Exception thrown");
    assertEquals("Pipeline 10 does not exist in program 4.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecution_missingExecution() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution("4", "3", "10"), "Exception thrown for 404");
    assertEquals(String.format("Cannot get execution: %s/api/program/4/pipeline/3/execution/10 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecution_via_pipeline() throws CloudManagerApiException {
    Pipeline pipeline = underTest.listPipelines("4", p -> p.getId().equals("3")).get(0);
    PipelineExecution execution = pipeline.getExecution("1");
    assertEquals("1", execution.getId(), "Execution Id matches");
    assertEquals("3", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("4", execution.getProgramId(), "Program Id matches");
  }

  @Test
  void cancelExecution_buildRunning() throws CloudManagerApiException {
    underTest.cancelExecution("4", "3", "1");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void cancelExecution_via_execution() throws CloudManagerApiException {
    Pipeline pipeline = underTest.listPipelines("4", p -> p.getId().equals("3")).get(0);
    PipelineExecution execution = pipeline.getExecution("1");

    execution.cancel();
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/1/phase/4596/step/8492/cancel").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void cancelExecution_codeQualityWaiting() throws CloudManagerApiException {
    underTest.cancelExecution("4", "3", "2");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/2/phase/4596/step/8493/cancel").withContentType(MediaType.APPLICATION_JSON));
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
  }

  @Test
  void cancelExecution_deploylWaiting() throws CloudManagerApiException {
    underTest.cancelExecution("4", "3", "5");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/5/phase/8567/step/15492/advance").withContentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void cancelExecution_deploylWaiting_errorState() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution("4", "3", "6"), "Exception thrown");
    assertEquals("Cannot find a cancel link for the current step (deploy). Step may not be cancellable.", exception.getMessage(), "Message was correct");
  }

  @Test
  void advanceCurrentExecution_missingPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.advanceCurrentExecution("4", "10"), "Exception thrown");
    assertEquals("Pipeline 10 does not exist in program 4.", exception.getMessage(), "Message was correct");
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
  }

  @Test
  void advanceExecution_approvalWaiting() throws CloudManagerApiException {
    underTest.advanceExecution("4", "3", "4");
    client.verify(request().withMethod("PUT").withPath("/api/program/4/pipeline/3/execution/4/phase/8567/step/15490/advance").withContentType(MediaType.APPLICATION_JSON));
  }
}



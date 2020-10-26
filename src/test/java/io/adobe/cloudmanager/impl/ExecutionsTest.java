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
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockServerExtension.class)
public class ExecutionsTest extends AbstractApiTest {

  @Test
  void getCurrentExecution_executionReturns404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getCurrentExecution("5", "5"), "Exception thrown for 404");
    assertEquals(String.format("Cannot get execution: %s/api/program/5/pipeline/5/execution (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getCurrentExecution_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getCurrentExecution("5", "6");
    assertEquals("1000",execution.getId(), "Execution Id matches");
    assertEquals("6", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("5", execution.getProgramId(), "Program Id matches");
  }

  @Test
  void getExecution_missingPipeline() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution("5", "100", "1001"), "Exception thrown for 404");
    assertEquals("Pipeline 100 does not exist in program 5.", exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecution_missingExecution() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution("5", "5", "1002"), "Exception thrown for 404");
    assertEquals(String.format("Cannot get execution: %s/api/program/5/pipeline/5/execution/1002 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getExecution_success() throws CloudManagerApiException {
    PipelineExecution execution = underTest.getExecution("5", "7", "1001");
    assertEquals("1001", execution.getId(), "Execution Id matches");
    assertEquals("7", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("5", execution.getProgramId(), "Program Id matches");
  }

  @Test
  void getExecution_via_pipeline() throws CloudManagerApiException {
    Pipeline pipeline = underTest.listPipelines("5", p -> p.getId().equals("7")).get(0);
    PipelineExecution execution = pipeline.getExecution("1001");
    assertEquals("1001",execution.getId(), "Execution Id matches");
    assertEquals("7", execution.getPipelineId(), "Pipeline Id matches");
    assertEquals("5", execution.getProgramId(), "Program Id matches");
  }
}



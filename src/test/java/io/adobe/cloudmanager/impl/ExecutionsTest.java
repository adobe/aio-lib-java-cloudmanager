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
import io.adobe.cloudmanager.swagger.model.PipelineExecution;
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
    assertEquals("1000",execution.getId(), "Id matches");
    assertEquals("5", execution.getProgramId(), "Program Id matches");
    assertEquals("6", execution.getPipelineId(), "Pipeline Id matches");
  }
}



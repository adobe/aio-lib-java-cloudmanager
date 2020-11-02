package io.adobe.cloudmanager.model;

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

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineUpdate;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
public class Pipeline extends io.adobe.cloudmanager.swagger.model.Pipeline {

  @Delegate
  private final io.adobe.cloudmanager.swagger.model.Pipeline delegate;
  @ToString.Exclude
  private final CloudManagerApi client;

  public Pipeline(io.adobe.cloudmanager.swagger.model.Pipeline delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  /**
   * Starts this pipeline.
   *
   * @return the new execution.
   * @throws CloudManagerApiException when any errors occur.
   */
  public PipelineExecution startExecution() throws CloudManagerApiException {
    return client.startExecution(this);
  }

  /**
   * Returns the specified execution.
   *
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  public PipelineExecution getExecution(String executionId) throws CloudManagerApiException {
    return client.getExecution(this, executionId);
  }

  /**
   * Updates this pipeline with the specified changes.
   *
   * @param update the updates to make to this pipeline
   * @return the updated Pipeline.
   * @throws CloudManagerApiException when any errors occur
   */
  public Pipeline update(PipelineUpdate update) throws CloudManagerApiException {
    return client.updatePipeline(this, update);
  }

  /**
   * Delete this pipeline.
   *
   * @throws CloudManagerApiException when any error occurs.
   */
  public void delete() throws CloudManagerApiException {
    client.deletePipeline(this);
  }
}

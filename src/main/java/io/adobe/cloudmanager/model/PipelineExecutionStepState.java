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

import java.io.OutputStream;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode
public class PipelineExecutionStepState extends io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState {

  @Delegate
  private final io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApi client;

  public PipelineExecutionStepState(io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  /**
   * Streams the default log, if any, for this step to the specified output stream.
   *
   * @param outputStream the output stream to write to
   * @throws CloudManagerApiException when any error occurs
   */
  public void getLog(OutputStream outputStream) throws CloudManagerApiException {
    client.getExecutionStepLog(this, null, outputStream);
  }

  /**
   * Streams the specified log, if any, for this step to the specified output stream.
   *
   * @param name         The name of the log to retrieve
   * @param outputStream the output stream to write to
   * @throws CloudManagerApiException when any error occurs
   */
  public void getLog(String name, OutputStream outputStream) throws CloudManagerApiException {
    client.getExecutionStepLog(this, name, outputStream);
  }
}

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

import java.io.OutputStream;
import java.util.function.Predicate;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode
public class PipelineExecutionStepStateImpl extends io.adobe.cloudmanager.generated.model.PipelineExecutionStepState implements PipelineExecutionStepState {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.generated.model.PipelineExecutionStepState delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApiImpl client;

  public PipelineExecutionStepStateImpl(io.adobe.cloudmanager.generated.model.PipelineExecutionStepState delegate, CloudManagerApiImpl client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Status getStatusState() {
    return Status.fromValue(getStatus().getValue());
  }

  @Override
  public PipelineExecution getExecution() throws CloudManagerApiException {
    return client.getExecution(this);
  }

  @Override
  public void getLog(OutputStream outputStream) throws CloudManagerApiException {
    client.downloadExecutionStepLog(this, null, outputStream);
  }

  @Override
  public void getLog(String name, OutputStream outputStream) throws CloudManagerApiException {
    client.downloadExecutionStepLog(this, name, outputStream);
  }

  /**
   * Predicate for pipelines based on they are the current execution.
   */
  public static final Predicate<io.adobe.cloudmanager.generated.model.PipelineExecutionStepState> IS_CURRENT = (stepState ->
      stepState.getStatus() != io.adobe.cloudmanager.generated.model.PipelineExecutionStepState.StatusEnum.FINISHED
  );

  /**
   * Predicate for pipelines that are in a waiting state.
   */
  public static final Predicate<io.adobe.cloudmanager.generated.model.PipelineExecutionStepState> IS_WAITING = (stepState ->
      stepState.getStatus() == io.adobe.cloudmanager.generated.model.PipelineExecutionStepState.StatusEnum.WAITING
  );

}

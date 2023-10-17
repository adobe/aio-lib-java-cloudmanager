package io.adobe.cloudmanager.impl.pipeline;

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

import java.util.Optional;
import java.util.Set;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineApi;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionApi;
import io.adobe.cloudmanager.PipelineUpdate;
import io.adobe.cloudmanager.Variable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class PipelineImpl extends io.adobe.cloudmanager.impl.generated.Pipeline implements Pipeline {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Pipeline delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineApi client;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionApi executionClient;

  public PipelineImpl(io.adobe.cloudmanager.impl.generated.Pipeline delegate, PipelineApi client, PipelineExecutionApi executionClient) {
    this.delegate = delegate;
    this.client = client;
    this.executionClient = executionClient;
  }

  @Override
  public Status getStatusState() {
    return Status.valueOf(getStatus().getValue());
  }

  @Override
  public void delete() throws CloudManagerApiException {
    client.delete(this);
  }

  @Override
  public Pipeline update(PipelineUpdate update) throws CloudManagerApiException {
    return client.update(this, update);
  }

  @Override
  public void invalidateCache() throws CloudManagerApiException {
    client.invalidateCache(this);
  }

  @Override
  public Optional<PipelineExecution> getCurrentExecution() throws CloudManagerApiException {
    return executionClient.getCurrent(this);
  }

  @Override
  public PipelineExecution startExecution() throws CloudManagerApiException {
    return executionClient.start(this);
  }

  @Override
  public PipelineExecution getExecution(String executionId) throws CloudManagerApiException {
    return executionClient.get(this, executionId);
  }

  @Override
  public Set<Variable> getVariables() throws CloudManagerApiException {
    return client.getVariables(this);
  }

  @Override
  public Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException {
    return client.setVariables(this, variables);
  }
}

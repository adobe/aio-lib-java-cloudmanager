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

import java.util.Set;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineUpdate;
import io.adobe.cloudmanager.Variable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode
public class PipelineImpl extends io.adobe.cloudmanager.impl.generated.Pipeline implements Pipeline {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Pipeline delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApi client;

  public PipelineImpl(io.adobe.cloudmanager.impl.generated.Pipeline delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Status getStatusState() {
    return Status.fromValue(getStatus().getValue());
  }

  @Override
  public PipelineExecution startExecution() throws CloudManagerApiException {
    return client.startExecution(this);
  }

  @Override
  public PipelineExecution getExecution(String executionId) throws CloudManagerApiException {
    return client.getExecution(this, executionId);
  }

  @Override
  public Pipeline update(PipelineUpdate update) throws CloudManagerApiException {
    return client.updatePipeline(this, update);
  }

  @Override
  public void delete() throws CloudManagerApiException {
    client.deletePipeline(this);
  }

  @Override
  public Set<Variable> listVariables() throws CloudManagerApiException {
    return client.listPipelineVariables(this);
  }

  @Override
  public Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException {
    return client.setPipelineVariables(this, variables);
  }

  @Override
  public String getSelfLink() {
    return getLinks().getSelf().getHref();
  }
}

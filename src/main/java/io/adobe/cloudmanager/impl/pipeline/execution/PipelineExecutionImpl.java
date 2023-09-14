package io.adobe.cloudmanager.impl.pipeline.execution;

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

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionApi;
import io.adobe.cloudmanager.impl.CloudManagerApiImpl;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class PipelineExecutionImpl extends io.adobe.cloudmanager.impl.generated.PipelineExecution implements PipelineExecution {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.PipelineExecution delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionApiImpl client;

  public PipelineExecutionImpl(io.adobe.cloudmanager.impl.generated.PipelineExecution delegate, PipelineExecutionApiImpl client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public void advance() throws CloudManagerApiException {
    client.internalAdvance(this);
  }

  @Override
  public void cancel() throws CloudManagerApiException {
    client.internalCancel(this);
  }

  @Override
  public Status getStatusState() {
    return Status.fromValue(getStatus().getValue());
  }
}

package io.adobe.cloudmanager.impl.pipeline.execution;

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

import java.time.OffsetDateTime;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionEndEvent;
import io.adobe.cloudmanager.impl.generated.event.PipelineExecutionEndEventEvent;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = false)
public class PipelineExecutionEndEventImpl implements PipelineExecutionEndEvent {

  private final PipelineExecutionEndEventEvent delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionApiImpl client;

  public PipelineExecutionEndEventImpl(PipelineExecutionEndEventEvent delegate, PipelineExecutionApiImpl client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public String getId() {
    return delegate.getAtId();
  }

  @Override
  public OffsetDateTime getPublished() {
    return delegate.getActivitystreamspublished();
  }

  @Override
  public PipelineExecution getExecution() throws CloudManagerApiException {
    return client.get(delegate.getActivitystreamsobject());
  }

}

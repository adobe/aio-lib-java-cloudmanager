package com.adobe.aio.cloudmanager.impl.pipeline.execution;

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

import org.apache.commons.io.FilenameUtils;

import com.adobe.aio.cloudmanager.Artifact;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.PipelineExecutionApi;
import com.adobe.aio.cloudmanager.PipelineExecutionStepState;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ArtifactImpl implements Artifact {

  private final com.adobe.aio.cloudmanager.impl.generated.Artifact delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionApi client;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionStepState step;

  public ArtifactImpl(com.adobe.aio.cloudmanager.impl.generated.Artifact delegate, PipelineExecutionApi client, PipelineExecutionStepState step) {
    this.delegate = delegate;
    this.client = client;
    this.step = step;
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public String getFileName() {
    return FilenameUtils.getName(delegate.getFile());
  }

  @Override
  public String getType() {
    return delegate.getType().name();
  }

  @Override
  public String getMd5() {
    return delegate.getMd5();
  }

  @Override
  public String getDownloadUrl() throws CloudManagerApiException {
    return client.getArtifactDownloadUrl(step, getId());
  }
}

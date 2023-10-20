package com.adobe.aio.cloudmanager.impl.content;

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

import com.adobe.aio.cloudmanager.ContentFlow;
import com.adobe.aio.cloudmanager.ContentSetApi;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.impl.generated.ContentFlowResultDetails;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

import static com.adobe.aio.cloudmanager.Environment.*;

public class ContentFlowImpl extends com.adobe.aio.cloudmanager.impl.generated.ContentFlow implements ContentFlow {

  private static final long serialVersionUID = 1L;

  @Delegate
  private final com.adobe.aio.cloudmanager.impl.generated.ContentFlow delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final ContentSetApi client;

  private Results exportResults;
  private Results importResults;

  public ContentFlowImpl(com.adobe.aio.cloudmanager.impl.generated.ContentFlow delegate, ContentSetApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Tier getEnvironmentTier() {
    return Tier.valueOf(delegate.getTier().toUpperCase());
  }

  @Override
  public Status getFlowStatus() {
    return Status.valueOf(getStatus().toUpperCase().replaceAll(" ", "_"));
  }

  @Override
  public Results getExportResults() {
    if (exportResults == null) {
      ContentFlowResultDetails cfrd = delegate.getResultDetails().getExportResult();
      exportResults = new Results(cfrd.getErrorCode(), cfrd.getMessage(), cfrd.getDetails());
    }
    return exportResults;
  }

  @Override
  public Results getImportResults() {
    if (importResults == null) {
      ContentFlowResultDetails cfrd = delegate.getResultDetails().getImportResult();
      importResults = new Results(cfrd.getErrorCode(), cfrd.getMessage(), cfrd.getDetails());

    }
    return importResults;
  }

  @Override
  public void cancel() throws CloudManagerApiException {
    client.cancelFlow(getDestProgramId(), getId());
  }
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.adobe.aio.cloudmanager.ContentFlow;
import com.adobe.aio.cloudmanager.ContentSet;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class ContentSetImpl extends com.adobe.aio.cloudmanager.impl.generated.ContentSet implements ContentSet {

  private static final long serialVersionUID = 1L;

  @Delegate
  private com.adobe.aio.cloudmanager.impl.generated.ContentSet delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final ContentSetApiImpl client;

  private Collection<PathDefinition> pathDefinitions;

  public ContentSetImpl(com.adobe.aio.cloudmanager.impl.generated.ContentSet delegate, ContentSetApiImpl client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Collection<PathDefinition> getPathDefinitions() {
    if (pathDefinitions == null) {
      pathDefinitions = new ArrayList<>();
      delegate.getPaths().forEach(p -> pathDefinitions.add(new PathDefinition(p.getPath(), new HashSet<>(p.getExcluded()))));
    }
    return pathDefinitions;
  }

  @Override
  public void update(String name, String description, Collection<PathDefinition> definitions) throws CloudManagerApiException {
    delegate = client.internalUpdate(getProgramId(), getId(), name, description, definitions);
    pathDefinitions = null;
  }

  @Override
  public void delete() throws CloudManagerApiException {
    client.delete(getProgramId(), getId());
  }

  @Override
  public ContentFlow startFlow(String srcEnvironmentId, String destEnvironment, boolean includeAcl) throws CloudManagerApiException {
    return client.startFlow(getProgramId(), getId(), srcEnvironmentId, destEnvironment, includeAcl);
  }
}

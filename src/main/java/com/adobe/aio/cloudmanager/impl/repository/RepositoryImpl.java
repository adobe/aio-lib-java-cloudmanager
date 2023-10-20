package com.adobe.aio.cloudmanager.impl.repository;

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

import java.util.Collection;

import com.adobe.aio.cloudmanager.impl.generated.Repository;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.RepositoryApi;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class RepositoryImpl extends Repository implements com.adobe.aio.cloudmanager.Repository {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final Repository delegate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final RepositoryApi client;

  public RepositoryImpl(Repository delegate, RepositoryApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public String getName() {
    return delegate.getRepo();
  }

  @Override
  public String getUrl() {
    return delegate.getRepositoryUrl();
  }

  @Override
  public Collection<String> listBranches() throws CloudManagerApiException {
    return client.listBranches(this);
  }
}

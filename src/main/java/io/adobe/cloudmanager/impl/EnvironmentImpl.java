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

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.EnvironmentLog;
import io.adobe.cloudmanager.LogOption;
import io.adobe.cloudmanager.impl.generated.HalLink;
import io.adobe.cloudmanager.Variable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode
public class EnvironmentImpl extends io.adobe.cloudmanager.impl.generated.Environment implements io.adobe.cloudmanager.Environment {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Environment delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApi client;

  public EnvironmentImpl(io.adobe.cloudmanager.impl.generated.Environment delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Collection<LogOption> getLogOptions() {
    return getAvailableLogOptions().stream().map(LogOptionImpl::new).collect(Collectors.toList());
  }

  @Override
  public void delete() throws CloudManagerApiException {
    client.deleteEnvironment(this);
  }

  @Override
  public String getDeveloperConsoleUrl() throws CloudManagerApiException {
    HalLink link = delegate.getLinks().getHttpnsAdobeComadobecloudreldeveloperConsole();
    if (link == null) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.NO_DEVELOPER_CONSOLE, getId(), getProgramId());
    } else {
      return link.getHref();
    }
  }

  @Override
  public String getSelfLink() {
    return getLinks().getSelf().getHref();
  }

  public Set<Variable> getVariables() throws CloudManagerApiException {
    return client.listEnvironmentVariables(this);
  }

  public Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException {
    return client.setEnvironmentVariables(this, variables);
  }

  @Override
  public Collection<EnvironmentLog> downloadLogs(LogOption logOption, int days, File dir) throws CloudManagerApiException {
    return client.downloadLogs(this, logOption, days, dir);
  }
}

package io.adobe.cloudmanager.impl.environment;

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
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.EnvironmentApi;
import io.adobe.cloudmanager.EnvironmentLog;
import io.adobe.cloudmanager.LogOption;
import io.adobe.cloudmanager.Variable;
import io.adobe.cloudmanager.impl.generated.HalLink;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class EnvironmentImpl extends io.adobe.cloudmanager.impl.generated.Environment implements Environment {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Environment delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final EnvironmentApi client;

  public EnvironmentImpl(io.adobe.cloudmanager.impl.generated.Environment delegate, EnvironmentApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Collection<LogOption> getLogOptions() {
    return getAvailableLogOptions().stream().map(LogOptionImpl::new).collect(Collectors.toList());
  }

  @Override
  public void delete() throws CloudManagerApiException {
    delete(false);
  }

  @Override
  public void delete(boolean ignoreFailure) throws CloudManagerApiException {
    client.delete(this, ignoreFailure);
  }

  @Override
  public Collection<EnvironmentLog> listLogs(LogOption option, int days) throws CloudManagerApiException {
    return client.listLogs(this, option, days);
  }

  @Override
  public String getLogDownloadUrl(LogOption option, LocalDate date) throws CloudManagerApiException {
    return client.getLogDownloadUrl(this, option, date);
  }

  @Override
  public String getDeveloperConsoleUrl() throws CloudManagerApiException {
    HalLink link = delegate.getLinks().getHttpnsAdobeComadobecloudreldeveloperConsole();
    if (link == null) {
      throw new CloudManagerApiException(String.format("Environment %s does not appear to support Developer Console.", getId()));
    } else {
      return link.getHref();
    }
  }

  public Set<Variable> getVariables() throws CloudManagerApiException {
    return client.getVariables(this);
  }

  public Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException {
    return client.setVariables(this, variables);
  }

  @Override
  public Collection<EnvironmentLog> downloadLogs(LogOption logOption, int days, File dir) throws CloudManagerApiException {
//    return client.downloadLogs(this, logOption, days, dir);
    return null;
  }
}

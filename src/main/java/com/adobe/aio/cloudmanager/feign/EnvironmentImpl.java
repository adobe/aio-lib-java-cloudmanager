package com.adobe.aio.cloudmanager.feign;

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

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Environment;
import com.adobe.aio.cloudmanager.EnvironmentLog;
import com.adobe.aio.cloudmanager.LogOption;
import com.adobe.aio.cloudmanager.Variable;
import com.adobe.aio.cloudmanager.impl.model.HalLink;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class EnvironmentImpl extends com.adobe.aio.cloudmanager.impl.model.Environment implements Environment {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final com.adobe.aio.cloudmanager.impl.model.Environment delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApiImpl client;

  public EnvironmentImpl(com.adobe.aio.cloudmanager.impl.model.Environment delegate, CloudManagerApiImpl client) {
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
      throw new CloudManagerApiException(String.format("Environment %s does not appear to support Developer Console.", getId()));
    } else {
      return link.getHref();
    }
  }

  public Set<Variable> listVariables() throws CloudManagerApiException {
    return client.listEnvironmentVariables(this);
  }

  public Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException {
    return client.setEnvironmentVariables(this, variables);
  }

  @Override
  public Collection<EnvironmentLog> downloadLogs(LogOption logOption, int days, File dir) throws CloudManagerApiException {
    return client.downloadEnvironmentLogs(this, logOption, days, dir);
  }
}

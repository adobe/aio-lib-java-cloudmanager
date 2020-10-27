package io.adobe.cloudmanager.model;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 Adobe Inc.
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

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.swagger.model.HalLink;
import lombok.ToString;
import lombok.experimental.Delegate;

public class Environment extends io.adobe.cloudmanager.swagger.model.Environment {

  public Environment(io.adobe.cloudmanager.swagger.model.Environment delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Delegate
  private final io.adobe.cloudmanager.swagger.model.Environment delegate;

  @ToString.Exclude
  private final CloudManagerApi client;

  public void delete() throws CloudManagerApiException {
    client.deleteEnvironment(this);
  }

  public String getDeveloperConsoleUrl() throws CloudManagerApiException {
    HalLink link = delegate.getLinks().getHttpnsAdobeComadobecloudreldeveloperConsole();
    if (link == null) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.NO_DEVELOPER_CONSOLE, getId(), getProgramId());
    } else {
      return link.getHref();
    }
  }
}

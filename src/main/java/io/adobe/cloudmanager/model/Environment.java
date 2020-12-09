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

import java.io.File;
import java.util.List;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.swagger.model.HalLink;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode
public class Environment extends io.adobe.cloudmanager.swagger.model.Environment {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.swagger.model.Environment delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final CloudManagerApi client;

  public Environment(io.adobe.cloudmanager.swagger.model.Environment delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  /**
   * Delete this program.
   *
   * @throws CloudManagerApiException when any error occurs.
   */
  public void delete() throws CloudManagerApiException {
    client.deleteEnvironment(this);
  }

  /**
   * Retrieve the Developer Console URL for this Environment.
   *
   * @return the url to the developer console.
   * @throws CloudManagerApiException when any error occurs
   */
  public String getDeveloperConsoleUrl() throws CloudManagerApiException {
    HalLink link = delegate.getLinks().getHttpnsAdobeComadobecloudreldeveloperConsole();
    if (link == null) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.NO_DEVELOPER_CONSOLE, getId(), getProgramId());
    } else {
      return link.getHref();
    }
  }

  /**
   * Lists the variables configured in this environment.
   *
   * @return the list of variables
   * @throws CloudManagerApiException when any error occurs.
   */
  public List<Variable> getVariables() throws CloudManagerApiException {
    return client.getEnvironmentVariables(this);
  }

  /**
   * Sets the specified variables on this environment.
   *
   * @param variables the variables to set
   * @return the complete list of variables in this environment
   * @throws CloudManagerApiException when any error occurs.
   */
  public List<Variable> setVariables(Variable... variables) throws CloudManagerApiException {
    return client.setEnvironmentVariables(this, variables);
  }

  /**
   * Downloads the logs for this environment
   *
   * @param service the service context for the logs
   * @param name    the name of the log in the service
   * @param days    the number of days to download
   * @param dir     the directory in which to place the log files
   * @return a list of EnvironmentLogs with details about the downloaded files
   * @throws CloudManagerApiException when any error occurs.
   */
  public List<EnvironmentLog> downloadLogs(String service, String name, int days, File dir) throws CloudManagerApiException {
    return client.downloadLogs(this, service, name, days, dir);
  }
}

package io.adobe.cloudmanager;

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
import java.util.List;

public interface Environment {

  /**
   * The id of this environment
   *
   * @return the id
   */
  String getId();

  /**
   * The id of the program context for the environment.
   *
   * @return the program id
   */
  String getProgramId();

  /**
   * The name of this environment
   *
   * @return the name
   */
  String getName();

  /**
   * Link to this environment.
   *
   * @return the link to this environment.
   */
  String getSelfLink();

  /**
   * A list of available log options for downloading.
   *
   * @return list of log options
   */
  List<LogOption> getLogOptions();

  /**
   * Delete this program.
   *
   * @throws CloudManagerApiException when any error occurs.
   */
  void delete() throws CloudManagerApiException;

  /**
   * Retrieve the Developer Console URL for this Environment.
   *
   * @return the url to the developer console.
   * @throws CloudManagerApiException when any error occurs
   */
  String getDeveloperConsoleUrl() throws CloudManagerApiException;

  /**
   * Lists the variables configured in this environment.
   *
   * @return the list of variables
   * @throws CloudManagerApiException when any error occurs.
   */
  List<Variable> getVariables() throws CloudManagerApiException;

  /**
   * Sets the specified variables on this environment.
   *
   * @param variables the variables to set
   * @return the complete list of variables in this environment
   * @throws CloudManagerApiException when any error occurs.
   */
  List<Variable> setVariables(Variable... variables) throws CloudManagerApiException;

  /**
   * Downloads the logs for this environment
   *
   * @param logOption the log file reference
   * @param days      the number of days to download
   * @param dir       the directory in which to place the log files
   * @return a list of EnvironmentLogs with details about the downloaded files
   * @throws CloudManagerApiException when any error occurs.
   */
  List<EnvironmentLog> downloadLogs(LogOption logOption, int days, File dir) throws CloudManagerApiException;

}

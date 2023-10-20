package com.adobe.aio.cloudmanager;

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

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.adobe.aio.cloudmanager.exception.DeleteInProgressException;

/**
 * An Environment definition.
 */
public interface Environment {

  /**
   * The id of this environment
   *
   * @return the id
   */
  String getId();

  /**
   * The id of the program context of this environment.
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
   * The type of this environment
   *
   * @return type
   */
  Type getEnvType();

  /**
   * A list of available log options for downloading.
   *
   * @return list of log options
   */
  Collection<LogOption> getLogOptions();

  /**
   * Delete this environment.
   *
   * @throws DeleteInProgressException if the delete operation is already active
   * @throws CloudManagerApiException when any error occurs
   */
  void delete() throws DeleteInProgressException, CloudManagerApiException;

  /**
   * Delete this environment, with option to ignore resource deletion failure.
   *
   * @param ignoreFailure flag to ignore failures
   * @throws DeleteInProgressException if the delete operation is already active
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(boolean ignoreFailure) throws DeleteInProgressException, CloudManagerApiException;

  /**
   * List available logs of the specified type, for the number of days.
   *
   * @param option the log option details
   * @param days   number of days of logs to list
   * @return the list of logs
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<EnvironmentLog> listLogs(@NotNull LogOption option, int days) throws CloudManagerApiException;

  /**
   * Retrieve the fully qualified URL to the log file for download.
   *
   * @param option the type of logs to download
   * @param date   the date of the logs to download
   * @return the log file download url
   * @throws CloudManagerApiException when any error occurs
   */
  String getLogDownloadUrl(@NotNull LogOption option, @NotNull LocalDate date) throws CloudManagerApiException;

  /**
   * List the deployment regions for this environment
   *
   * @return list of deployments
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<RegionDeployment> listRegionDeployments() throws CloudManagerApiException;

  /**
   * Add a deployment to the specified region.
   *
   * @param region the region
   * @throws CloudManagerApiException when any error occurs
   */
  void addRegionDeployment(Region region) throws CloudManagerApiException;

  /**
   * Remove the the the deployment from the specified region.
   *
   * @param region the region
   * @throws CloudManagerApiException when any error occurs
   */
  void removeRegionDeployment(Region region) throws CloudManagerApiException;

  /**
   * List the variables configured in this environment.
   *
   * @return the set of variables
   * @throws CloudManagerApiException when any error occurs
   */
  Set<Variable> getVariables() throws CloudManagerApiException;

  /**
   * Set the variables on this environment.
   *
   * @param variables the variables to set
   * @return the complete list of variables in this environment
   * @throws CloudManagerApiException when any error occurs
   */
  Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException;

  /**
   * Reset this environment, if it is of type RDE. Any other type of environment results in a NOOP
   *
   * @throws CloudManagerApiException when any error occurs
   */
  void reset() throws CloudManagerApiException;

  /**
   * Retrieve the Developer Console URL for this Environment.
   *
   * @return the url to the developer console.
   * @throws CloudManagerApiException when any error occurs
   */
  String getDeveloperConsoleUrl() throws CloudManagerApiException;

  /**
   * Download the logs for this environment, to the specified directory.
   *
   * @param logOption the log file reference
   * @param days      the number of days to download
   * @param dir       the directory in which to place the log files
   * @return a list of EnvironmentLogs with details about the downloaded files
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<EnvironmentLog> downloadLogs(LogOption logOption, int days, File dir) throws CloudManagerApiException;

  /**
   * Representation of the different environment types.
   */
  enum Type {
    DEV,
    STAGE,
    PROD,
    RDE
  }

  /**
   * Tiers within an Environment
   */
  enum Tier {
    AUTHOR,
    PREVIEW,
    PUBLISH
  }

  /**
   * Predicate to use for retrieving an environment based on its name. Match is exact.
   */
  final class NamePredicate implements Predicate<Environment> {

    private final String name;

    public NamePredicate(String name) {
      this.name = name;
    }

    @Override
    public boolean test(Environment environment) {
      return StringUtils.equals(name, environment.getName());
    }

    @Override
    public String toString() {
      return String.format("Name='%s'", name);
    }
  }
}

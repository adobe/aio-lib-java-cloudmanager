package io.adobe.cloudmanager;

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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.validation.constraints.NotNull;

import io.adobe.cloudmanager.exception.DeleteInProgressException;

/**
 * Environment API
 */
public interface EnvironmentApi {

  /**
   * List all environments in the program.
   *
   * @param programId the program id
   * @return list of environments
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Environment> list(@NotNull String programId) throws CloudManagerApiException;

  /**
   * List all environments in the program, of the specified type.
   *
   * @param programId the program id
   * @param type      the type of environments to list
   * @return list of environments
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Environment> list(@NotNull String programId, Environment.Type type) throws CloudManagerApiException;

  /**
   * Create a new environment in the program.
   *
   * @param programId   the program in which to create the environment
   * @param name        the name of the new environment
   * @param type        the type of environment to create
   * @param region      the region in which to create the environment
   * @param description optional description of the environment
   * @return the newly created environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Environment create(@NotNull String programId, @NotNull String name, @NotNull Environment.Type type, @NotNull String region, String description) throws CloudManagerApiException;

  /**
   * Retrieve the environment within the program context.
   *
   * @param programId     the program id
   * @param environmentId the environment id
   * @return the environment
   * @throws CloudManagerApiException when any error occurs
   */
  Environment get(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Delete the environment in the specified program.
   *
   * @param programId     the program id of the environment context
   * @param environmentId the environment to delete
   * @throws DeleteInProgressException if the delete operation is already active
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId, @NotNull String environmentId) throws DeleteInProgressException, CloudManagerApiException;

  /**
   * Delete the environment.
   *
   * @param environment the environment to delete
   * @throws DeleteInProgressException if the delete operation is already active
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull Environment environment) throws DeleteInProgressException, CloudManagerApiException;

  /**
   * Delete the environment, with option to ignore resource deletion failure.
   *
   * @param programId     the program id of the environment context
   * @param environmentId the environment to delete
   * @param ignoreFailure flag to ignore failures
   * @throws DeleteInProgressException if the delete operation is already active
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId, @NotNull String environmentId, boolean ignoreFailure) throws DeleteInProgressException, CloudManagerApiException;

  /**
   * Delete the environment.
   *
   * @param environment   the environment to delete
   * @param ignoreFailure flag to ignore failures
   * @throws DeleteInProgressException if the delete operation is already active
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull Environment environment, boolean ignoreFailure) throws DeleteInProgressException, CloudManagerApiException;

  /**
   * List logs of the specified type for the environment.
   *
   * @param programId     the program id for the environment
   * @param environmentId the environment id
   * @param option        the type of logs to list
   * @param days          then number of days of logs to list
   * @return the list of environment logs
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<EnvironmentLog> listLogs(@NotNull String programId, @NotNull String environmentId, @NotNull LogOption option, int days) throws CloudManagerApiException;

  /**
   * List logs of the specified type for the environment.
   *
   * @param environment the environment
   * @param option      the type of logs to list
   * @param days        then number of days of logs to list
   * @return the list of environment logs
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<EnvironmentLog> listLogs(@NotNull Environment environment, @NotNull LogOption option, int days) throws CloudManagerApiException;

  /**
   * Get the fully qualified URL to the log file for download.
   *
   * @param programId     the program id for the environment
   * @param environmentId the environment id
   * @param option        the type of logs to download
   * @param date          the date of the logs to download
   * @return the log file download url
   * @throws CloudManagerApiException when any error occurs
   */
  String getLogDownloadUrl(@NotNull String programId, @NotNull String environmentId, @NotNull LogOption option, @NotNull LocalDate date) throws CloudManagerApiException;

  /**
   * Get the fully qualified URL to the log file for download.
   *
   * @param environment the environment
   * @param option      the type of logs to download
   * @param date        the date of the logs to download
   * @return the log file download url
   * @throws CloudManagerApiException when any error occurs
   */
  String getLogDownloadUrl(@NotNull Environment environment, @NotNull LogOption option, @NotNull LocalDate date) throws CloudManagerApiException;

  /**
   * Get the region deployment.
   *
   * @param programId     the program id of the deployment
   * @param environmentId the environment id of the deployment
   * @param deploymentId  the id of the deployment
   * @return the region deployment details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  RegionDeployment getRegionDeployment(@NotNull String programId, @NotNull String environmentId, @NotNull String deploymentId) throws CloudManagerApiException;

  /**
   * Get the region deployment.
   *
   * @param programId     the program id context
   * @param environmentId the environment id context
   * @return the region deployment details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<RegionDeployment> listRegionDeployments(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Get the region deployment.
   *
   * @param environment the environment context
   * @return the region deployment details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<RegionDeployment> listRegionDeployments(@NotNull Environment environment) throws CloudManagerApiException;

  /**
   * Create a new deployment in the specified region.
   *
   * @param environment the environment to update
   * @param regions     the regions in which to deploy
   * @throws CloudManagerApiException when any error occurs
   */
  void createRegionDeployments(@NotNull Environment environment, @NotNull Region... regions) throws CloudManagerApiException;

  /**
   * Remove the deployment from the specified region.
   *
   * @param environment the environment to update
   * @param regions     the regions from which to remove the deployment
   * @throws CloudManagerApiException when any error occurs
   */
  void removeRegionDeployments(@NotNull Environment environment, @NotNull Region... regions) throws CloudManagerApiException;

  /**
   * List all variables associated with the environment.
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @return set of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> getVariables(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * List all variables associated with the environment.
   *
   * @param environment the environment
   * @return set of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> getVariables(@NotNull Environment environment) throws CloudManagerApiException;

  /**
   * Set the variables in the environment.
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @param variables     the variables to set
   * @return updated list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> setVariables(@NotNull String programId, @NotNull String environmentId, Variable... variables) throws CloudManagerApiException;

  /**
   * Set the variables in the environment.
   *
   * @param environment the environment context
   * @param variables   the variables to set
   * @return updated list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> setVariables(@NotNull Environment environment, Variable... variables) throws CloudManagerApiException;

  /**
   * Reset the Rapid Development Environment. If the environment is not an RDE, result is undefined.
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @throws CloudManagerApiException when any error occurs
   */
  void resetRde(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Reset the Rapid Development Environment. If the environment is not an RDE, result is undefined.
   *
   * @param environment the environment
   * @throws CloudManagerApiException when any error occurs
   */
  void resetRde(@NotNull Environment environment) throws CloudManagerApiException;

  // TODO: Need Details about Restore Points

  // Convenience Methods

  /**
   * Returns the first environment which matches the predicate, within the program context.
   *
   * @param programId the program id
   * @param predicate environment filter
   * @return the environment
   * @throws CloudManagerApiException when any error occurs
   */
  Optional<Environment> get(@NotNull String programId, @NotNull Predicate<Environment> predicate) throws CloudManagerApiException;

  /**
   * Downloads the logs for the environment, to the specified folder.
   *
   * @param programId     the program id context for the environment
   * @param environmentId the environment id
   * @param logOption     the log file reference
   * @param days          how many days of log files to retrieve
   * @param dir           the directory in which to save the files
   * @return a list of EnvironmentLogs with details about the downloaded files
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<EnvironmentLog> downloadLogs(@NotNull String programId, @NotNull String environmentId, @NotNull LogOption logOption, int days, @NotNull File dir) throws CloudManagerApiException;

  /**
   * Downloads the logs for the environment, to the specified folder.
   *
   * @param environment the environment context
   * @param logOption   the log file reference
   * @param days        how many days of log files to retrieve
   * @param dir         the directory in which to save the files
   * @return a list of EnvironmentLogs with details about the downloaded files
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<EnvironmentLog> downloadLogs(@NotNull Environment environment, @NotNull LogOption logOption, int days, @NotNull File dir) throws CloudManagerApiException;
}

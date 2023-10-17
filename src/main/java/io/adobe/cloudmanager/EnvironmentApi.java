package io.adobe.cloudmanager;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import javax.validation.constraints.NotNull;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.impl.environment.EnvironmentApiImpl;

public interface EnvironmentApi {

  /**
   * Lists all environments in the specified program.
   *
   * @param programId the program id
   * @return list of environments
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Environment> list(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Lists all environments in the specified program, of the specified type
   *
   * @param programId the program id
   * @param type      the type of environments to list
   * @return list of environments
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Environment> list(@NotNull String programId, Environment.Type type) throws CloudManagerApiException;

  /**
   * Creates a new environment in the specified program.
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
   * Returns the specified environment in the program context
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
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Delete the specified environment.
   *
   * @param environment the environment to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull Environment environment) throws CloudManagerApiException;

  /**
   * Delete the specified environment, with option to ignore resource deletion failure.
   *
   * @param programId     the program id of the environment context
   * @param environmentId the environment to delete
   * @param ignoreFailure flag to ignore failures
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull String programId, @NotNull String environmentId, boolean ignoreFailure) throws CloudManagerApiException;

  /**
   * Delete the specified environment.
   *
   * @param environment   the environment to delete
   * @param ignoreFailure flag to ignore failures
   * @throws CloudManagerApiException when any error occurs
   */
  void delete(@NotNull Environment environment, boolean ignoreFailure) throws CloudManagerApiException;

  /**
   * Lists logs of the specified type for the environment
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
   * Lists logs of the specified type for the environment
   *
   * @param environment the environment
   * @param option      the type of logs to list
   * @param days        then number of days of logs to list
   * @return the list of environment logs
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<EnvironmentLog> listLogs(@NotNull Environment environment, @NotNull LogOption option, int days) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
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
   * Returns the fully qualified URL to the log file for download.
   *
   * @param environment the environment
   * @param option      the type of logs to download
   * @param date        the date of the logs to download
   * @return the log file download url
   * @throws CloudManagerApiException when any error occurs
   */
  String getLogDownloadUrl(@NotNull Environment environment, @NotNull LogOption option, @NotNull LocalDate date) throws CloudManagerApiException;

  /**
   * Returns the specified region deployment
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
   * Returns the specified region deployment.
   *
   * @param programId     the program id context
   * @param environmentId the environment id context
   * @return the region deployment details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<RegionDeployment> listRegionDeployments(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Returns the specified region deployment.
   *
   * @param environment the environment context
   * @return the region deployment details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<RegionDeployment> listRegionDeployments(@NotNull Environment environment) throws CloudManagerApiException;

  /**
   * Creates a new deployment in the specified region
   *
   * @param environment the environment to update
   * @param regions     the regions in which to deploy
   * @throws CloudManagerApiException when any error occurs
   */
  void createRegionDeployments(@NotNull Environment environment, @NotNull Region... regions) throws CloudManagerApiException;

  /**
   * Removes the deployment from the specified region
   *
   * @param environment the environment to update
   * @param regions     the regions from which to remove the deployment
   * @throws CloudManagerApiException when any error occurs
   */
  void removeRegionDeployments(@NotNull Environment environment, @NotNull Region... regions) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified environment
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @return set of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> getVariables(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified environment
   *
   * @param environment the environment
   * @return set of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> getVariables(@NotNull Environment environment) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the environment.
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
   * Sets the specified variables in the environment.
   *
   * @param environment the environment context
   * @param variables   the variables to set
   * @return updated list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> setVariables(@NotNull Environment environment, Variable... variables) throws CloudManagerApiException;

  /**
   * Resets the specified Rapid Development Environment. If the specified environment is not an RDE, result is undefined.
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @throws CloudManagerApiException when any error occurs
   */
  void resetRde(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Resets the specified Rapid Development Environment. If the specified environment is not an RDE, result is undefined.
   *
   * @param environment the environment
   * @throws CloudManagerApiException when any error occurs
   */
  void resetRde(@NotNull Environment environment) throws CloudManagerApiException;


  // TODO: Need Details about Restore Points


  // Convenience Methods

  /**
   * Downloads the logs for the specified environment.
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
   * Downloads the logs for the specified environment.
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


  /**
   * Create an Environment API builder.
   *
   * @return a builder.
   */
  static Builder builder() {
    return new Builder();
  }

  /**
   * Builds instances of the CloudManager API.
   */
  class Builder {
    private Workspace workspace;
    private URL url;

    private Builder() {
    }

    public Builder workspace(@NotNull Workspace workspace) {
      this.workspace = workspace;
      return this;
    }

    public Builder url(@NotNull URL url) {
      this.url = url;
      return this;
    }

    public EnvironmentApi build() {
      if (workspace == null) {
        throw new IllegalStateException("Workspace must be specified.");
      }
      if (workspace.getAuthContext() == null) {
        throw new IllegalStateException("Workspace must specify AuthContext");
      }
      workspace.getAuthContext().validate();
      return new EnvironmentApiImpl(workspace, url);
    }
  }
}

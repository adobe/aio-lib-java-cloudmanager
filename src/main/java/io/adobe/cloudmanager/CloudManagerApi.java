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
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.validation.constraints.NotNull;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.event.PipelineExecutionEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import io.adobe.cloudmanager.impl.CloudManagerApiImpl;
import lombok.NonNull;

/**
 * API for interacting with Cloud Manager AdobeIO endpoints.
 * <p>
 * Implementations of this interface must be context aware of the Adobe IMS Org when making API calls.
 *
 * @since 1.0
 * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/">Cloud Manager API</a>
 */
public interface CloudManagerApi {

  public static final String CLOUD_MANAGER_URL = "https://cloudmanager.adobe.io";

  /**
   * Create a CloudManager API builder.
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

    public Builder() {
    }

    public Builder workspace(@NotNull Workspace workspace) {
      this.workspace = workspace;
      return this;
    }

    public Builder url(@NotNull URL url) {
      this.url = url;
      return this;
    }

    public CloudManagerApi build() {
      if (workspace == null) {
        throw new IllegalStateException("Workspace must be specified.");
      }
      if (workspace.getAuthContext() == null) {
        throw new IllegalStateException("Workspace must specify AuthContext");
      }
      workspace.getAuthContext().validate();
      return new CloudManagerApiImpl(workspace, url);
    }
  }

  // Try to keep the APIs in the order they are listed on the Reference Docs
  // Helper APIs come after the associated publicly defined ones
  // Reference: https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/

  /**
   * Returns the program with the specified id
   *
   * @param programId the id of the program
   * @return the program
   * @throws CloudManagerApiException when any error occurs
   */
  Program getProgram(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param programId the id of the program to delete.
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteProgram(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param program the program to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteProgram(@NotNull Program program) throws CloudManagerApiException;

  /**
   * List all programs for a Tenant
   *
   * @param tenantId the id tenant
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Program> listPrograms(@NotNull String tenantId) throws CloudManagerApiException;

  /**
   * List all programs for the Tenant
   *
   * @param tenant the tenant
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Program> listPrograms(@NotNull Tenant tenant) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program.
   *
   * @param programId the program id
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Repository> listRepositories(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program.
   *
   * @param program the program
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Repository> listRepositories(@NotNull Program program) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, up to the defined limit.
   *
   * @param programId the program id
   * @param limit the maximum number of repositories to retrieve
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull
  Collection<Repository> listRepositories(@NonNull String programId, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, up to the defined limit.
   *
   * @param program the program
   * @param limit the maximum number of repositories to retrieve
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull
  Collection<Repository> listRepositories(@NonNull Program program, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, from the starting position, up to the defined limit.
   *
   * @param programId the program id
   * @param start the starting position in the list to retrieve
   * @param limit the maximum number of repositories to retrieve
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull
  Collection<Repository> listRepositories(@NonNull String programId, int start, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, from the starting position, up to the defined limit.
   *
   * @param program the program
   * @param start the starting position in the list to retrieve
   * @param limit the maximum number of repositories to retrieve
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull
  Collection<Repository> listRepositories(@NonNull Program program, int start, int limit) throws CloudManagerApiException;

  /**
   * Get a specific repository in the program.
   *
   * @param programId the program id
   * @param repositoryId the repository id
   * @return the repository
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull
  Repository getRepository(@NonNull String programId, @NonNull String repositoryId) throws CloudManagerApiException;

  /**
   * Get a specific repository in the program.
   *
   * @param program the program
   * @param repositoryId the repository id
   * @return the repository
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull
  Repository getRepository(@NonNull Program program, @NonNull String repositoryId) throws CloudManagerApiException;

  /**
   * Lists all the branches associated with the repository.
   *
   * @param repository the repository
   * @return branches
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getBranches">List Branches API</a>
   */
  @NonNull Collection<String> listBranches(@NonNull Repository repository) throws CloudManagerApiException;



  /**
   * Lists all pipelines within the specified program.
   *
   * @param programId the program id
   * @return the list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Pipeline> listPipelines(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Lists all pipelines in the program that meet the predicate clause.
   *
   * @param programId the program id
   * @param predicate a predicate used to filter the pipelines
   * @return a list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Pipeline> listPipelines(@NotNull String programId, @NotNull Predicate<Pipeline> predicate) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the id of the pipeline to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void deletePipeline(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param pipeline the pipeline to delete.
   * @throws CloudManagerApiException when any error occurs
   */
  void deletePipeline(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param programId  the program id for pipeline context
   * @param pipelineId the id of the pipeline to change
   * @param updates    the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Pipeline updatePipeline(@NotNull String programId, @NotNull String pipelineId, @NotNull PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param pipeline the pipeline to update
   * @param updates  the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Pipeline updatePipeline(@NotNull Pipeline pipeline, @NotNull PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Returns an optional current execution of the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id of to find the execution
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Optional<PipelineExecution> getCurrentExecution(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrentExecution(String, String)} may not return a result.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   *
   */
  @NotNull
  PipelineExecution startExecution(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrentExecution(String, String)} may not return a result.
   *
   * @param pipeline the {@link Pipeline} to start
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution startExecution(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param pipeline    the pipeline context for the execution
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution getExecution(@NotNull Pipeline pipeline, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the pipeline id
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution getExecution(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the Pipeline Execution associated with the AdobeIO Execution Start event
   * @param event the pipeline start event
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution getExecution(@NotNull PipelineExecutionStartEvent event) throws CloudManagerApiException;

  /**
   * Returns the Pipeline Execution associated with the AdobeIO Execution End event
   * @param event the pipeline end event
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution getExecution(@NotNull PipelineExecutionEndEvent event) throws CloudManagerApiException;

  /**
   * Indicates if the specified pipeline execution is running. No assumptions are made about it's state - only that it has ended.
   *
   * @param execution the execution context to check
   * @return true if the pipeline is running, false if has ended
   * @throws CloudManagerApiException when any error occurs
   */
  boolean isExecutionRunning(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Indicates if the specified pipeline execution is running. No assumptions are made about it's state - only that it has ended.
   *
   * @param programId   the program context for the pipeline execution
   * @param pipelineId  the pipeline id for the execution
   * @param executionId the execution id
   * @return true if the pipeline is running, false if ended
   * @throws CloudManagerApiException when any error occurs
   */
  boolean isExecutionRunning(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified action step for the pipeline execution
   *
   * @param execution the execution context
   * @param action    the step state action
   * @return the step state details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getExecutionStepState(@NotNull PipelineExecution execution, @NotNull String action) throws CloudManagerApiException;

  /**
   * Gets the step state for based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getExecutionStepState(@NotNull PipelineExecutionStepStartEvent event) throws CloudManagerApiException;

  /**
   * Gets the step state for based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getExecutionStepState(@NotNull PipelineExecutionStepWaitingEvent event) throws CloudManagerApiException;

  /**
   * Gets the step state for based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getExecutionStepState(@NotNull PipelineExecutionStepEndEvent event) throws CloudManagerApiException;

  /**
   * Gets the current step for the execution.
   *
   * @param execution the pipeline execution
   * @return the current step
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getCurrentStep(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Gets the waiting step for the execution.
   *
   * @param execution the pipeline execution
   * @return the waiting step
   * @throws CloudManagerApiException when any error occurs or waiting step is not found
   */
  @NotNull
  PipelineExecutionStepState getWaitingStep(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be advanced
   * @throws CloudManagerApiException when any error occurs
   */
  void advanceExecution(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be advanced
   * @throws CloudManagerApiException when any error occurs
   */
  void advanceExecution(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Advances the current execution of the specified pipeline. If no current execution exists, quietly does nothing.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs
   */
  void advanceCurrentExecution(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be canceled
   * @throws CloudManagerApiException when any error occurs
   */
  void cancelExecution(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be canceled
   * @throws CloudManagerApiException when any error occurs
   */
  void cancelExecution(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Cancels the current execution of the specified pipeline. If no current execution exists, quietly does nothing.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs
   */
  void cancelCurrentExecution(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getExecutionStepLogDownloadUrl(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId, @NotNull String action) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @param name         custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getExecutionStepLogDownloadUrl(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId, @NotNull String action, @NotNull String name) throws CloudManagerApiException;


  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param execution    the execution for the log
   * @param action       the execution step action for the log
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getExecutionStepLogDownloadUrl(@NotNull PipelineExecution execution, @NotNull String action) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param execution    the execution for the log
   * @param action       the execution step action for the log
   * @param name         custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getExecutionStepLogDownloadUrl(@NotNull PipelineExecution execution, @NotNull String action, @NotNull String name) throws CloudManagerApiException;


  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   * <p>
   * Uses the default log file name for the step.
   *
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs
   */
  void downloadExecutionStepLog(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId, @NotNull String action, @NotNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @param name         custom log file name
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs
   */
  void downloadExecutionStepLog(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId, @NotNull String action, @NotNull String name, @NotNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param execution    the execution for the log
   * @param action       the step action for the log
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs
   */
  void downloadExecutionStepLog(@NotNull PipelineExecution execution, @NotNull String action, @NotNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param execution    the execution for the log
   * @param action       the step action for the log
   * @param filename     the log file name or {@code null} for the default
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs
   */
  void downloadExecutionStepLog(@NotNull PipelineExecution execution, @NotNull String action, @NotNull String filename, @NotNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Retrieves the metrics for the specified execution and step, if any.
   *
   * @param execution the execution step
   * @param action    the action step for which quality metrics are desired
   * @return the metrics for the execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Metric> getQualityGateResults(@NotNull PipelineExecution execution, @NotNull String action) throws CloudManagerApiException;

  /**
   * Lists all environments in the specified program.
   *
   * @param programId the program id
   * @return list of environments in the program
   * @throws CloudManagerApiException when any error occurs
   */
  Collection<Environment> listEnvironments(@NotNull String programId) throws CloudManagerApiException;

  /**
   * Delete the environment in the specified program.
   *
   * @param programId     the program id of the environment context
   * @param environmentId the environment to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteEnvironment(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Delete the specified environment.
   *
   * @param environment the environment to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteEnvironment(@NotNull Environment environment) throws CloudManagerApiException;

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
   * Lists all variables associated with the specified environment
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @return set of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> listEnvironmentVariables(@NotNull String programId, @NotNull String environmentId) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified environment
   *
   * @param environment the environment
   * @return set of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> listEnvironmentVariables(@NotNull Environment environment) throws CloudManagerApiException;

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
  Set<Variable> setEnvironmentVariables(@NotNull String programId, @NotNull String environmentId, Variable... variables) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the environment.
   *
   * @param environment the environment context
   * @param variables   the variables to set
   * @return updated list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> setEnvironmentVariables(@NotNull Environment environment, Variable... variables) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified pipeline
   *
   * @param programId  the program id of the pipeline
   * @param pipelineId the pipeline id
   * @return set of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> listPipelineVariables(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified pipeline
   *
   * @param pipeline the pipeline context
   * @return set of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> listPipelineVariables(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the pipeline
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the pipeline id
   * @param variables  the variables to set
   * @return updated list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> setPipelineVariables(@NotNull String programId, @NotNull String pipelineId, Variable... variables) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the pipeline
   *
   * @param pipeline  the pipeline context
   * @param variables the variables to set
   * @return updated list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Set<Variable> setPipelineVariables(@NotNull Pipeline pipeline, Variable... variables) throws CloudManagerApiException;

  /**
   * Lists the tenants associated with the IMS Org in the API Context
   *
   * @return list of tenants
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Tenant> listTenants() throws CloudManagerApiException;

  /**
   * Gets the tenant with the specified identifier.
   *
   * @param tenantId the id of the tenant
   * @return the tenant
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Tenant getTenant(@NotNull String tenantId) throws CloudManagerApiException;
}

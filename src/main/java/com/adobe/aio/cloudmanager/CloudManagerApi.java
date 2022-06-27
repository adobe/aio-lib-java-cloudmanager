package com.adobe.aio.cloudmanager;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.adobe.aio.cloudmanager.event.PipelineExecutionEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import lombok.NonNull;

public interface CloudManagerApi {

  String BASE_URL = "https://cloudmanager.adobe.io";
  String META_SCOPE = "/s/ent_cloudmgr_sdk";

  /**
   * List all programs in the organization
   *
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getPrograms">List Programs API</a>
   * @deprecated Use {@link #listPrograms(String)} instead.
   */
  @NonNull Collection<Program> listPrograms() throws CloudManagerApiException;

  /**
   * Get a specific program in the organization
   *
   * @param programId the id of the program
   * @return the {@link Program}
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getProgram">Get Program API</a>
   */
  @NonNull Program getProgram(String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param programId the id of the program to delete.
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/deleteProgram">Delete Program API</a>
   */
  void deleteProgram(@NonNull String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param program the program to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/deleteProgram">Delete Program API</a>
   */
  void deleteProgram(@NonNull Program program) throws CloudManagerApiException;

  /**
   * Lists all the programs for the specified tenant.
   * 
   * @param tenantId the tenant id 
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getProgramsForTenant">List Programs API</a>
   */
  Collection<Program> listPrograms(String tenantId) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program.
   * 
   * @param programId the program id
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getRepositories">List Repositories API</a>
   */
  @NonNull Collection<Repository> listRepositories(@NonNull String programId) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program.
   *
   * @param program the program
   * @return list of repositories
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getRepositories">List Repositories API</a>
   */
  @NonNull Collection<Repository> listRepositories(@NonNull Program program) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, up to the defined limit.
   *
   * @param programId the program id
   * @param limit the maximum number of repositories to retrieve
   * @return list of repositories
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getRepositories">List Repositories API</a>
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull Collection<Repository> listRepositories(@NonNull String programId, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, up to the defined limit.
   *
   * @param program the program
   * @param limit the maximum number of repositories to retrieve
   * @return list of repositories
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getRepositories">List Repositories API</a>
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull Collection<Repository> listRepositories(@NonNull Program program, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, from the starting position, up to the defined limit. 
   *
   * @param programId the program id
   * @param start the starting position in the list to retrieve
   * @param limit the maximum number of repositories to retrieve
   * @return list of repositories
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getRepositories">List Repositories API</a>
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull Collection<Repository> listRepositories(@NonNull String programId, int start, int limit) throws CloudManagerApiException;

  /**
   * Lists all repositories for the specified program, from the starting position, up to the defined limit.
   *
   * @param program the program
   * @param start the starting position in the list to retrieve
   * @param limit the maximum number of repositories to retrieve
   * @return list of repositories
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getRepository">List Repositories API</a>
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull Collection<Repository> listRepositories(@NonNull Program program, int start, int limit) throws CloudManagerApiException;

  /**
   * Get a specific repository in the program.
   * 
   * @param programId the program id
   * @param repositoryId the repository id
   * @return the repository
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getRepositories">List Repositories API</a>
   */
  @NonNull Repository getRepository(@NonNull String programId, @NonNull String repositoryId) throws CloudManagerApiException;

  /**
   * Get a specific repository in the program.
   *
   * @param program the program
   * @param repositoryId the repository id
   * @return the repository
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getRepositories">List Repositories API</a>
   */
  @NonNull Repository getRepository(@NonNull Program program, @NonNull String repositoryId) throws CloudManagerApiException;

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
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getPipelines">List Pipelines API</a>
   */
  @NonNull Collection<Pipeline> listPipelines(@NonNull String programId) throws CloudManagerApiException;

  /**
   * Lists all pipelines in the program that meet the predicate clause.
   *
   * @param programId the program id
   * @param predicate a predicate used to filter the pipelines
   * @return a list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getPipelines">List Pipelines API</a>
   */
  @NonNull Collection<Pipeline> listPipelines(@NonNull String programId, @NonNull Predicate<Pipeline> predicate) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the id of the pipeline to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/deletePipeline">Delete Pipeline API</a>
   */
  void deletePipeline(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param pipeline the pipeline to delete.
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/deletePipeline">Delete Pipeline API</a>
   */
  void deletePipeline(@NonNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param programId  the program id for pipeline context
   * @param pipelineId the id of the pipeline to change
   * @param updates    the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/patchPipeline">Patch Pipeline API</a>
   */
  @NonNull Pipeline updatePipeline(@NonNull String programId, @NonNull String pipelineId, @NonNull PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param pipeline the pipeline to update
   * @param updates  the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/patchPipeline">Patch Pipeline API</a>
   */
  @NonNull Pipeline updatePipeline(@NonNull Pipeline pipeline, @NonNull PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Invalidates the build cache (Maven) for the specified pipeline.
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the id of the pipeline to invalidate
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/invalidateCache">Invalidate Pipeline Cache API</a>
   */
  void invalidatePipelineCache(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Invalidates the build cache (Maven) for the specified pipeline.
   *
   * @param pipeline the pipeline context
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/invalidateCache">Invalidate Pipeline Cache API</a>
   */
  void invalidatePipelineCache(@NonNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Returns an optional current execution of the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id of to find the execution
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getCurrentExecution">Get Current Pipeline Execution API</a>
   */
  @NonNull Optional<PipelineExecution> getCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Returns an optional current execution of the specified pipeline.
   *
   * @param pipeline the pipeline reference
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getCurrentExecution">Get Current Pipeline Execution API</a>
   */
  @NonNull Optional<PipelineExecution> getCurrentExecution(@NonNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrentExecution(String, String)} may not return a result.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/startPipeline">Start Pipeline API</a>
   */
  @NonNull PipelineExecution startExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrentExecution(String, String)} may not return a result.
   *
   * @param pipeline the {@link Pipeline} to start
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/startPipeline">Start Pipeline API</a>
   */
  @NonNull PipelineExecution startExecution(@NonNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the pipeline id
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getExecution">Get Pipeline Execution API</a>
   */
  @NonNull PipelineExecution getExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param pipeline    the pipeline context for the exectuion
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getExecution">Get Pipeline Execution API</a>
   */
  @NonNull PipelineExecution getExecution(@NonNull Pipeline pipeline, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the Pipeline Execution associated with the AdobeIO Execution Start event
   *
   * @param event the pipeline start event
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getExecution">Get Pipeline Execution API</a>*
   */
  @NonNull PipelineExecution getExecution(@NonNull PipelineExecutionStartEvent event) throws CloudManagerApiException;

  /**
   * Returns the Pipeline Execution associated with the AdobeIO Execution End event
   *
   * @param event the pipeline end event
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getExecution">Get Pipeline Execution API</a>
   */
  @NonNull PipelineExecution getExecution(@NonNull PipelineExecutionEndEvent event) throws CloudManagerApiException;

  /**
   * Indicates if the specified pipeline execution is running. No assumptions are made about it's state - only that it has ended.
   *
   * @param programId   the program context for the pipeline execution
   * @param pipelineId  the pipeline id for the execution
   * @param executionId the execution id
   * @return true if the pipeline is running, false if it has ended
   * @throws CloudManagerApiException when any error occurs
   */
  boolean isExecutionRunning(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Indicates if the specified pipeline execution is running. No assumptions are made about it's state - only that it has ended.
   *
   * @param execution the execution context to check
   * @return true if the pipeline is running, false if it has ended
   * @throws CloudManagerApiException when any error occurs
   */
  boolean isExecutionRunning(@NonNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Returns the specified action step for the pipeline execution
   *
   * @param execution the execution context
   * @param action    the step state action
   * @return the step state details
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/stepState">Get Step State API</a>
   */
  @NonNull PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException;

  /**
   * Gets the step state for execution based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/stepState">Get Step State API</a>
   */
  @NonNull PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepStartEvent event) throws CloudManagerApiException;

  /**
   * Gets the step state for execution based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/stepState">Get Step State API</a>
   */
  @NonNull PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepWaitingEvent event) throws CloudManagerApiException;

  /**
   * Gets the step state for execution based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/stepState">Get Step State API</a>
   */
  @NonNull PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepEndEvent event) throws CloudManagerApiException;

  /**
   * Gets the current step for the execution.
   *
   * @param execution the pipeline execution
   * @return the current step
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/stepState">Get Step State API</a>
   */
  @NonNull PipelineExecutionStepState getCurrentStep(@NonNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Gets the waiting step for the execution.
   *
   * @param execution the pipeline execution
   * @return the waiting step
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/stepState">Get Step State API</a>
   */
  @NonNull PipelineExecutionStepState getWaitingStep(@NonNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Advances the current execution of the specified pipeline. If no current execution exists, quietly does nothing.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/advancePipelineExecution">Advance Pipeline API</a>
   */
  void advanceCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be advanced
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/advancePipelineExecution">Advance Pipeline API</a>
   */
  void advanceExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be advanced
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/advancePipelineExecution">Advance Pipeline API</a>
   */
  void advanceExecution(@NonNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Cancels the current execution of the specified pipeline. If no current execution exists, quietly does nothing.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/cancelPipelineExecutionStep">Cancel Pipeline API</a>
   */
  void cancelCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be canceled
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/cancelPipelineExecutionStep">Cancel Pipeline API</a>
   */
  void cancelExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be canceled
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/cancelPipelineExecutionStep">Cancel Pipeline API</a>
   */
  void cancelExecution(@NonNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param programId   the program id of the pipeline context
   * @param pipelineId  the pipeline id for the execution context
   * @param executionId the execution id for the logs
   * @param action      the execution step action for the log
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getStepLogs">Adobe Cloud Manager API</a>
   */
  @NonNull String getExecutionStepLogDownloadUrl(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param programId   the program id of the pipeline context
   * @param pipelineId  the pipeline id for the execution context
   * @param executionId the execution id for the logs
   * @param action      the execution step action for the log
   * @param name        custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getStepLogs">Adobe Cloud Manager API</a>
   */
  @NonNull String getExecutionStepLogDownloadUrl(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull String name) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param execution the execution for the log
   * @param action    the execution step action for the log
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getStepLogs">Adobe Cloud Manager API</a>
   */
  @NonNull String getExecutionStepLogDownloadUrl(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @param execution the execution for the log
   * @param action    the execution step action for the log
   * @param name      custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getStepLogs">Adobe Cloud Manager API</a>
   */
  @NonNull String getExecutionStepLogDownloadUrl(@NonNull PipelineExecution execution, @NonNull String action, @NonNull String name) throws CloudManagerApiException;

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
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getStepLogs">Get Execution Logs API</a>
   */
  void downloadExecutionStepLog(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @param filename     custom log file name
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getStepLogs">Get Execution Logs API</a>
   */
  void downloadExecutionStepLog(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull String filename, @NonNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param execution    the execution for the log
   * @param action       the step action for the log
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getStepLogs">Get Execution Logs API</a>
   */
  void downloadExecutionStepLog(@NonNull PipelineExecution execution, @NonNull String action, @NonNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param execution    the execution for the log
   * @param action       the step action for the log
   * @param filename     the log file name or {@code null} for the default
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getStepLogs">Get Execution Logs API</a>
   */
  void downloadExecutionStepLog(@NonNull PipelineExecution execution, @NonNull String action, @NonNull String filename, @NonNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Retrieves the metrics for the specified execution and step, if any.
   *
   * @param execution the execution step
   * @param action    the action step for which quality metrics are desired
   * @return the metrics for the execution
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/stepMetric">Get Step Metrics API</a>
   */
  @NonNull Collection<Metric> getQualityGateResults(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException;

  /**
   * Provides an iterator for iterating executions for the specified pipeline, newest to oldest.
   *
   * @param pipeline the pipeline context
   * @return iterator of pipeline executions
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getExecutions">Get Step Metrics API</a>
   */
  @NonNull Iterator<PipelineExecution> listExecutions(@NonNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   *Provides an iterator for iterating executions for the specified pipeline, newest to oldest. Each fetch will use the specified limit.
   *
   * @param pipeline the pipeline context
   * @param limit the number of executions to retrieve per API call
   * @return iterator of pipeline executions
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getExecutions">Get Step Metrics API</a>
   */
  @NonNull Iterator<PipelineExecution> listExecutions(@NonNull Pipeline pipeline, int limit) throws CloudManagerApiException;

  /**
   * Lists all environments in the specified program.
   *
   * @param programId the program id
   * @return list of environments in the program
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getEnvironments">List Environments API</a>
   */
  Collection<Environment> listEnvironments(@NonNull String programId) throws CloudManagerApiException;

  /**
   * Lists all environments in the specified program, by their type.
   *
   * @param programId the program id
   * @param type      the type of environments to retrieve
   * @return list of environments in the program
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getEnvironments">List Environments API</a>
   */
  Collection<Environment> listEnvironments(@NonNull String programId, Environment.Type type) throws CloudManagerApiException;

  /**
   * Retrieve an environment from the specified program based on the provided predicate.
   *
   * @param programId the program id
   * @param predicate predicate for selecting environment
   * @return the first Environment that matches the predicate
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getEnvironments">List Environments API</a>
   */
  @NonNull Environment getEnvironment(@NonNull String programId, Predicate<Environment> predicate) throws CloudManagerApiException;

  /**
   * Delete the environment in the specified program.
   *
   * @param programId     the program id of the environment context
   * @param environmentId the environment to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/deleteEnvironment">Delete Environment API</a>
   */
  void deleteEnvironment(@NonNull String programId, @NonNull String environmentId) throws CloudManagerApiException;

  /**
   * Delete the specified environment.
   *
   * @param environment the environment to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/deleteEnvironment">Delete Environment API</a>
   */
  void deleteEnvironment(@NonNull Environment environment) throws CloudManagerApiException;

  /**
   * Downloads the logs for the specified environment.
   *
   * @param programId     the program id context for the environment
   * @param environmentId the environment id
   * @param logOption     the log file reference
   * @param days          how many days of log files to retrieve
   * @param dir           the directory in which to save the files
   * @return a list of EnvironmentLogs with details about the downloaded files
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/downloadLogs">Download Environment Logs API</a>
   */
  @NonNull Collection<EnvironmentLog> downloadEnvironmentLogs(@NonNull String programId, @NonNull String environmentId, @NonNull LogOption logOption, int days, @NonNull File dir) throws CloudManagerApiException;

  /**
   * Downloads the logs for the specified environment.
   *
   * @param environment the environment context
   * @param logOption   the log file reference
   * @param days        how many days of log files to retrieve
   * @param dir         the directory in which to save the files
   * @return a list of EnvironmentLogs with details about the downloaded files
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/downloadLogs">Download Environment Logs API</a>
   */
  @NonNull Collection<EnvironmentLog> downloadEnvironmentLogs(@NonNull Environment environment, @NonNull LogOption logOption, int days, @NonNull File dir) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified environment
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @return list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getEnvironmentVariables">List User Environment Variables API</a>
   */
  @NonNull Set<Variable> listEnvironmentVariables(@NonNull String programId, @NonNull String environmentId) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified environment
   *
   * @param environment the environment
   * @return list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getEnvironmentVariables">List User Environment Variables API</a>
   */
  @NonNull Set<Variable> listEnvironmentVariables(@NonNull Environment environment) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the environment.
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @param variables     the variables to set
   * @return updated list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/patchEnvironmentVariables">Patch User Environment Variables</a>
   */
  @NonNull Set<Variable> setEnvironmentVariables(@NonNull String programId, @NonNull String environmentId, Variable... variables) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the environment.
   *
   * @param environment the environment context
   * @param variables   the variables to set
   * @return updated list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/patchEnvironmentVariables">Patch User Environment Variables</a>
   */
  @NonNull Set<Variable> setEnvironmentVariables(@NonNull Environment environment, Variable... variables) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified pipeline
   *
   * @param programId  the program id of the pipeline
   * @param pipelineId the pipeline id
   * @return list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getPipelineVariables">List User Pipeline Variables</a>
   */
  @NonNull Set<Variable> listPipelineVariables(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified pipeline
   *
   * @param pipeline the pipeline context
   * @return list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getPipelineVariables">List User Pipeline Variables</a>
   */
  @NonNull Set<Variable> listPipelineVariables(@NonNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the pipeline
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the pipeline id
   * @param variables  the variables to set
   * @return updated list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/patchPipelineVariables">Patch User Pipeline Variables</a>
   */
  @NonNull Set<Variable> setPipelineVariables(@NonNull String programId, @NonNull String pipelineId, Variable... variables) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the pipeline
   *
   * @param pipeline  the pipeline context
   * @param variables the variables to set
   * @return updated list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/patchPipelineVariables">Patch User Pipeline Variables</a>
   */
  @NonNull Set<Variable> setPipelineVariables(@NonNull Pipeline pipeline, Variable... variables) throws CloudManagerApiException;

}

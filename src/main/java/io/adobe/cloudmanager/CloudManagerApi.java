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
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import io.adobe.cloudmanager.generated.model.LogOptionRepresentation;
import io.adobe.cloudmanager.generated.model.PipelineStepMetrics;
import io.adobe.cloudmanager.model.EmbeddedProgram;
import io.adobe.cloudmanager.model.Environment;
import io.adobe.cloudmanager.model.EnvironmentLog;
import io.adobe.cloudmanager.model.Pipeline;
import io.adobe.cloudmanager.model.PipelineExecution;
import io.adobe.cloudmanager.model.PipelineExecutionStepState;
import io.adobe.cloudmanager.model.Variable;

/**
 * API for interacting with Cloud Manager AdobeIO endpoints.
 * <p>
 * Implementations of this interface must be context aware of the Adobe IMS Org when making API calls.
 *
 * @since 1.0
 */
public interface CloudManagerApi {

  // Try to keep the APIs in the order they are listed on the Reference Docs
  // Helper APIs come after the publicly defined ones
  // Reference: https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#!AdobeDocs/cloudmanager-api-docs/master/swagger-specs/api.yaml

  /**
   * List all programs in the organization
   *
   * @return a list of {@link EmbeddedProgram}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Programs/getPrograms">List Programs API</a>
   */
  List<EmbeddedProgram> listPrograms() throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param programId the id of the program to delete.
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Programs/deleteProgram">Delete Program API</a>
   */
  void deleteProgram(String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param program the program to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Programs/deleteProgram">Delete Program API</a>
   */
  void deleteProgram(EmbeddedProgram program) throws CloudManagerApiException;

  /**
   * Lists all pipelines within the specified program.
   *
   * @param programId the program id
   * @return the list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/getPipelines">List Pipelines API</a>
   */
  List<Pipeline> listPipelines(String programId) throws CloudManagerApiException;

  /**
   * Lists all pipelines in the program that meet the predicate clause.
   *
   * @param programId the program id
   * @param predicate a predicate used to filter the pipelines
   * @return a list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/getPipelines">List Pipelines API</a>
   */
  List<Pipeline> listPipelines(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the id of the pipeline to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/deletePipeline">Delete Pipeline API</a>
   */
  void deletePipeline(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param pipeline the pipeline to delete.
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/deletePipeline">Delete Pipeline API</a>
   */
  void deletePipeline(Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param programId  the program id for pipeline context
   * @param pipelineId the id of the pipeline to change
   * @param updates    the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/patchPipeline">Patch Pipeline API</a>
   */
  Pipeline updatePipeline(String programId, String pipelineId, PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param pipeline the pipeline to update
   * @param updates  the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/patchPipeline">Patch Pipeline API</a>
   */
  Pipeline updatePipeline(Pipeline pipeline, PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Returns an optional current execution of the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id of to find the execution
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getCurrentExecution">Get Current Pipeline Execution API</a>
   */
  Optional<PipelineExecution> getCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrentExecution(String, String)} may not return a result.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/startPipeline">Start Pipeline API</a>
   */
  PipelineExecution startExecution(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrentExecution(String, String)} may not return a result.
   *
   * @param pipeline the {@link Pipeline} to start
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/startPipeline">Start Pipeline API</a>
   */
  PipelineExecution startExecution(Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param pipeline    the pipeline context for the exectuion
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getExecution">Get Pipeline Execution API</a>
   */
  PipelineExecution getExecution(Pipeline pipeline, String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the pipeline id
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getExecution">Get Pipeline Execution API</a>
   */
  PipelineExecution getExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified action step for the pipeline execution
   *
   * @param execution the execution context
   * @param action    the step state action
   * @return the step state details
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  PipelineExecutionStepState getExecutionStepState(PipelineExecution execution, String action) throws CloudManagerApiException;

  /**
   * Gets the current step for the execution.
   *
   * @param execution the pipeline execution
   * @return the current step
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  PipelineExecutionStepState getCurrentStep(PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Gets the waiting step for the execution.
   *
   * @param execution the pipeline execution
   * @return the waiting step
   * @throws CloudManagerApiException when any error occurs or waiting step is not found
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  PipelineExecutionStepState getWaitingStep(PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be advanced
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/advancePipelineExecution">Advance Pipeline API</a>
   */
  void advanceExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be advanced
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/advancePipelineExecution">Advance Pipeline API</a>
   */
  void advanceExecution(PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Advances the current execution of the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs or if no current execution exists
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/advancePipelineExecution">Advance Pipeline API</a>
   */
  void advanceCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be canceled
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/cancelPipelineExecutionStep">Cancel Pipeline API</a>
   */
  void cancelExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be canceled
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/cancelPipelineExecutionStep">Cancel Pipeline API</a>
   */
  void cancelExecution(PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Cancels the current execution of the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs or if no current execution exists
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/cancelPipelineExecutionStep">Cancel Pipeline API</a>
   */
  void cancelCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException;

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
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Get Execution Logs API</a>
   */
  void getExecutionStepLog(String programId, String pipelineId, String executionId, String action, OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @param name         custom log file name
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Get Execution Logs API</a>
   */
  void getExecutionStepLog(String programId, String pipelineId, String executionId, String action, String name, OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param action       the execution step action for the log
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Get Execution Logs API</a>
   */
  void getExecutionStepLog(PipelineExecutionStepState action, OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param action       the execution step action for the log
   * @param name         the log file name or {@code null} for the default
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Get Execution Logs API</a>
   */
  void getExecutionStepLog(PipelineExecutionStepState action, String name, OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Retrieves the metrics for the specified execution step, if any.
   *
   * @param step the execution step
   * @return the metrics for the step
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepMetric">Get Step Metrics API</a>
   */
  PipelineStepMetrics getQualityGateResults(PipelineExecutionStepState step) throws CloudManagerApiException;

  /**
   * Lists all environments in the specified program.
   *
   * @param programId the program id
   * @return list of environments in the program
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Environments/getEnvironments">List Environments API</a>
   */
  List<Environment> listEnvironments(String programId) throws CloudManagerApiException;

  /**
   * Delete the environment in the specified program.
   *
   * @param programId     the program id of the environment context
   * @param environmentId the environment to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Environments/deleteEnvironment">Delete Environment API</a>
   */
  void deleteEnvironment(String programId, String environmentId) throws CloudManagerApiException;

  /**
   * Delete the specified environment.
   *
   * @param environment the environment to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Environments/deleteEnvironment">Delete Environment API</a>
   */
  void deleteEnvironment(Environment environment) throws CloudManagerApiException;

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
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Environments/downloadLogs">Download Environment Logs API</a>
   */
  List<EnvironmentLog> downloadLogs(String programId, String environmentId, LogOptionRepresentation logOption, int days, File dir) throws CloudManagerApiException;

  /**
   * Downloads the logs for the specified environment.
   *
   * @param environment the environment context
   * @param logOption   the log file reference
   * @param days        how many days of log files to retrieve
   * @param dir         the directory in which to save the files
   * @return a list of EnvironmentLogs with details about the downloaded files
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Environments/downloadLogs">Download Environment Logs API</a>
   */
  List<EnvironmentLog> downloadLogs(Environment environment, LogOptionRepresentation logOption, int days, File dir) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified environment
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @return list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Variables/getEnvironmentVariables">List User Environment Variables API</a>
   */
  List<Variable> listEnvironmentVariables(String programId, String environmentId) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified environment
   *
   * @param environment the environment
   * @return list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Variables/getEnvironmentVariables">List User Environment Variables API</a>
   */
  List<Variable> listEnvironmentVariables(Environment environment) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the environment.
   *
   * @param programId     the program id of the environment
   * @param environmentId the environment id
   * @param variables     the variables to set
   * @return updated list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Variables/patchEnvironmentVariables">Patch User Environment Variables</a>
   */
  List<Variable> setEnvironmentVariables(String programId, String environmentId, Variable... variables) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the environment.
   *
   * @param environment the environment context
   * @param variables   the variables to set
   * @return updated list of variables in the environment
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Variables/patchEnvironmentVariables">Patch User Environment Variables</a>
   */
  List<Variable> setEnvironmentVariables(Environment environment, Variable... variables) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified pipeline
   *
   * @param programId  the program id of the pipeline
   * @param pipelineId the pipeline id
   * @return list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Variables/getPipelineVariables">List User Pipeline Variables</a>
   */
  List<Variable> listPipelineVariables(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Lists all variables associated with the specified pipeline
   *
   * @param pipeline the pipeline context
   * @return list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Variables/getPipelineVariables">List User Pipeline Variables</a>
   */
  List<Variable> listPipelineVariables(Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the pipeline
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the pipeline id
   * @param variables  the variables to set
   * @return updated list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Variables/patchPipelineVariables">Patch User Pipeline Variables</a>
   */
  List<Variable> setPipelineVariables(String programId, String pipelineId, Variable... variables) throws CloudManagerApiException;

  /**
   * Sets the specified variables in the pipeline
   *
   * @param pipeline  the pipeline context
   * @param variables the variables to set
   * @return updated list of variables in the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Variables/patchPipelineVariables">Patch User Pipeline Variables</a>
   */
  List<Variable> setPipelineVariables(Pipeline pipeline, Variable... variables) throws CloudManagerApiException;

}

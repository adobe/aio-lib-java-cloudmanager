package io.adobe.cloudmanager;

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

import java.util.List;
import java.util.function.Predicate;

import io.adobe.cloudmanager.model.EmbeddedProgram;
import io.adobe.cloudmanager.model.Environment;
import io.adobe.cloudmanager.model.Pipeline;
import io.adobe.cloudmanager.model.PipelineExecution;
import io.adobe.cloudmanager.model.PipelineExecutionStepState;
import io.adobe.cloudmanager.model.Variable;
import io.adobe.cloudmanager.swagger.model.PipelineStepMetrics;

/**
 * API for interacting with Cloud Manager AdobeIO endpoints.
 * <p>
 * Implementations of this interface must be context aware of the Adobe IMS Org when making API calls.
 *
 * @since 1.0
 */
public interface CloudManagerApi {

  /**
   * List all programs in the organization
   *
   * @return a list of {@link EmbeddedProgram}s
   * @throws CloudManagerApiException when any error occurs
   */
  List<EmbeddedProgram> listPrograms() throws CloudManagerApiException;

  /**
   * Lists all pipelines within the specified program.
   *
   * @param programId the program id
   * @return the list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   */
  List<Pipeline> listPipelines(String programId) throws CloudManagerApiException;

  /**
   * Lists all pipelines in the program, that meed the predicate clause.
   *
   * @param programId the program id
   * @param predicate a predicate used to filter the pipelines
   * @return a list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   */
  List<Pipeline> listPipelines(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecution startExecution(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Starts the specified pipeline
   *
   * @param pipeline the {@link Pipeline} to start
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecution startExecution(Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Returns the current execution of the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id of to find the execution
   * @return the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecution getCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Cancels the current execution of the specified pipeline, if any execution exists.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs
   */
  void cancelCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Advances the current execution of the specified pipeline, if in an appropriate state.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs
   */
  void advanceCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param pipeline    the pipeline context for the exectuion
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
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
   */
  PipelineExecution getExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be canceled
   * @throws CloudManagerApiException when any error occurs
   */
  void cancelExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be canceled
   * @throws CloudManagerApiException when any error occurs
   */
  void cancelExecution(PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be advanced
   * @throws CloudManagerApiException when any error occurs
   */
  void advanceExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be advanced
   * @throws CloudManagerApiException when any error occurs
   */
  void advanceExecution(PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Gets the current step for the execution.
   *
   * @param execution the pipeline execution
   * @return the current step
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecutionStepState getCurrentStep(PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Gets the waiting step for the execution.
   *
   * @param execution the pipeline execution
   * @return the waiing step
   * @throws CloudManagerApiException when any error occurs or waiting step is not found
   */
  PipelineExecutionStepState getWaitingStep(PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Retrieves the metrics for the specified execution step, if any.
   *
   * @param step the execution step
   * @return the metrics for the step
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineStepMetrics getQualityGateResults(PipelineExecutionStepState step) throws CloudManagerApiException;

  /**
   * Lists all environments in the specified program.
   *
   * @param programId the program id
   * @return list of environments in the program
   * @throws CloudManagerApiException when any error occurs
   */
  List<Environment> listEnvironments(String programId) throws CloudManagerApiException;
/*
    Future<Void> getExecutionStepLog(String programId, String pipelineId, String executionId, StepStateAction action,
                                     String logFile, OutputStream outputStream);

    Future<List<DownloadedLog>> downloadLogs(String programId, String environmentId, Service service, LogName name, File dir);
*/

  /**
   * Delete the specified pipeline.
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the id of the pipeline to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void deletePipeline(String programId, String pipelineId) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param pipeline the pipeline to delete.
   * @throws CloudManagerApiException when any error occurs.
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
   */
  Pipeline updatePipeline(String programId, String pipelineId, PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param pipeline the pipeline to update
   * @param updates  the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  Pipeline updatePipeline(Pipeline pipeline, PipelineUpdate updates) throws CloudManagerApiException;

  List<Variable> getEnvironmentVariables(String programId, String environmentId) throws CloudManagerApiException;

  List<Variable> getEnvironmentVariables(Environment environment) throws CloudManagerApiException;

  List<Variable> setEnvironmentVariables(String programId, String environmentId, Variable... variables) throws CloudManagerApiException;

  List<Variable> setEnvironmentVariables(Environment environment, Variable... variables) throws CloudManagerApiException;
/*
    Future<List<Variable>> getPipelineVariables(String programId, String pipelineId);

    Future<Void> setPipelineVariables(String programId, String pipelineId, Variable ...variables);
*/

  /**
   * Delete the specified program.
   *
   * @param programId the id of the program to delete.
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteProgram(String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param program the program to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteProgram(EmbeddedProgram program) throws CloudManagerApiException;

  /**
   * Delete the environment in the specified program.
   *
   * @param programId     the program id of the environment context
   * @param environmentId the environment to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteEnvironment(String programId, String environmentId) throws CloudManagerApiException;

  /**
   * Delete the specified environment.
   *
   * @param environment the environment to delete
   * @throws CloudManagerApiException when any error occurs
   */
  void deleteEnvironment(Environment environment) throws CloudManagerApiException;

}

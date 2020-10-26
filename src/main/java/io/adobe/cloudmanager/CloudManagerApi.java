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
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.text.StringSubstitutor;

import io.adobe.cloudmanager.model.EmbeddedProgram;
import io.adobe.cloudmanager.model.Pipeline;
import io.adobe.cloudmanager.model.PipelineExecution;

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
   * Returns the specified execution of the pipeline.
   *
   * @param pipeline the pipeline context for the exectuion
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecution getExecution(Pipeline pipeline, String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param programId the program id context of the pipeline
   * @param pipelineId the pipeline id
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  PipelineExecution getExecution(String programId, String pipelineId, String executionId) throws CloudManagerApiException;

    /*
    Future<PipelineStepMetrics> getQualityGateResults(String programId, String pipelineId, String executionId,
                                                      StepStateAction action);

    Future<Void> cancelCurrentExecution(String programId, String pipelineId);

    Future<Void> advanceCurrentExecution(String programId, String pipelineId);

    Future<List<Environment>> listEnvironments(String programId);

    Future<Void> getExecutionStepLog(String programId, String pipelineId, String executionId, StepStateAction action,
                                     String logFile, OutputStream outputStream);

    Future<List<LogOptionRepresentation>> listAvailableLogOptions(String programId, String environmentId);

    Future<List<DownloadedLog>> downloadLogs(String programId, String environmentId, Service service, LogName name, File dir);
*/

  void deletePipeline(String programId, String pipelineId) throws CloudManagerApiException;

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


/*
    Future<String> getDeveloperConsoleUrl(String programId, String environmentId);

    Future<List<Variable>> getEnvironmentVariables(String programId, String environmentId);

    Future<Void> setEnvironmentVariables(String programId, String environmentId, Variable ...variables);

    Future<List<Variable>> getPipelineVariables(String programId, String pipelineId);

    Future<Void> setPipelineVariables(String programId, String pipelineId, Variable ...variables);

    Future<Void> deleteProgram(String programId);

    Future<Void> deleteEnvironment(String programId, String environmentId);
*/

}

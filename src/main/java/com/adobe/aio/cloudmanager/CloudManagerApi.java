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

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import com.adobe.aio.cloudmanager.event.PipelineExecutionEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepEndEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepStartEvent;
import com.adobe.aio.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import com.adobe.aio.cloudmanager.feign.CloudManagerApiImpl;
import com.adobe.aio.cloudmanager.feign.client.PipelineApiClient;
import com.adobe.aio.cloudmanager.feign.client.PipelineExecutionApiClient;
import com.adobe.aio.cloudmanager.feign.client.ProgramApiClient;
import com.adobe.aio.cloudmanager.feign.exception.PipelineExceptionDecoder;
import com.adobe.aio.cloudmanager.feign.exception.PipelineExecutionExceptionDecoder;
import com.adobe.aio.cloudmanager.feign.exception.ProgramExceptionDecoder;
import com.adobe.aio.feign.AIOHeaderInterceptor;
import com.adobe.aio.ims.feign.JWTAuthInterceptor;
import com.adobe.aio.workspace.Workspace;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.NonNull;

public interface CloudManagerApi {

  String BASE_URL = "https://cloudmanager.adobe.io";
  String META_SCOPE = "https://ims-na1.adobelogin.com/s/ent_cloudmgr_sdk";

  /**
   * Creates a new CloudManagerApi instance using the specified Workspace.
   * <p>
   * The workspace will be used for authentication and thus must pass a {@link Workspace#validateJwtCredentialConfig()}
   *
   * @param workspace the AIO Workspace Context.
   * @return an api instance
   */
  @NonNull
  static CloudManagerApi create(@NonNull Workspace workspace) {
    try {
      return create(workspace, new URL(BASE_URL));
    } catch (MalformedURLException ex) {
      // How did this happen?
      throw new IllegalStateException(ex.getMessage());
    }
  }

  /**
   * Creates a new CloudManagerApi instance using the specified Workspace and API base URL.
   * <p>
   * The workspace will be used for authentication and thus must pass a {@link Workspace#validateJwtCredentialConfig()}
   * <p>
   * This can be used to override the default Cloud Manager API endpoint.
   *
   * @param workspace the AIO Workspace Context.
   * @param url       the base URL for Cloud Manager's API
   * @return an api instance
   */
  @NonNull
  static CloudManagerApi create(@NonNull Workspace workspace, @NonNull URL url) {
    workspace.getMetascopes().add(META_SCOPE);
    workspace.validateJwtCredentialConfig();

    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    mapper.registerModule(new JavaTimeModule());

    Collection<RequestInterceptor> interceptors = new ArrayList<>();
    interceptors.add(JWTAuthInterceptor.builder().workspace(workspace).build());
    interceptors.add(AIOHeaderInterceptor.builder().workspace(workspace).build());

    Feign.Builder builder = Feign.builder()
        .client(new OkHttpClient())
        .requestInterceptors(interceptors)
        .encoder(new JacksonEncoder(mapper))
        .decoder(new JacksonDecoder(mapper))
        .logger(new Slf4jLogger())
        .logLevel(Logger.Level.FULL);

    ProgramApiClient programApi = builder.errorDecoder(new ProgramExceptionDecoder()).target(ProgramApiClient.class, url.toString());
    PipelineApiClient pipelineApi = builder.errorDecoder(new PipelineExceptionDecoder()).target(PipelineApiClient.class, url.toString());
    PipelineExecutionApiClient executionsApi = builder.errorDecoder(new PipelineExecutionExceptionDecoder()).target(PipelineExecutionApiClient.class, url.toString());
    return new CloudManagerApiImpl(programApi, pipelineApi, executionsApi);
  }

  /**
   * List all programs in the organization
   *
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getPrograms">List Programs API</a>
   */
  @NonNull
  Collection<Program> listPrograms() throws CloudManagerApiException;

  /**
   * Get a specific program in the organization
   *
   * @return the {@link Program}
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#operation/getProgram">Get Program API</a>
   */
  @NonNull
  Program getProgram(String programId) throws CloudManagerApiException;

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
   * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api#operation/deleteProgram">Delete Program API</a>
   */
  void deleteProgram(@NonNull Program program) throws CloudManagerApiException;

  // TODO: Add Repository APIs
  @NonNull
  Collection<Repository> listRepositories(@NonNull String programId) throws CloudManagerApiException;

  @NonNull
  Collection<Repository> listRepositories(@NonNull Program program) throws CloudManagerApiException;

  @NonNull
  Repository getRepository(@NonNull String programId, @NonNull String repositoryId) throws CloudManagerApiException;

  @NonNull
  Repository getRepository(@NonNull Program program, @NonNull String repositoryId) throws CloudManagerApiException;

  /**
   * Lists all pipelines within the specified program.
   *
   * @param programId the program id
   * @return the list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/getPipelines">List Pipelines API</a>
   */
  @NonNull
  Collection<Pipeline> listPipelines(@NonNull String programId) throws CloudManagerApiException;

  /**
   * Lists all pipelines in the program that meet the predicate clause.
   *
   * @param programId the program id
   * @param predicate a predicate used to filter the pipelines
   * @return a list of {@link Pipeline}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/getPipelines">List Pipelines API</a>
   */
  @NonNull
  Collection<Pipeline> listPipelines(@NonNull String programId, @NonNull Predicate<Pipeline> predicate) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param programId  the program context for the pipeline
   * @param pipelineId the id of the pipeline to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/deletePipeline">Delete Pipeline API</a>
   */
  void deletePipeline(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Delete the specified pipeline.
   *
   * @param pipeline the pipeline to delete.
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/deletePipeline">Delete Pipeline API</a>
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
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/patchPipeline">Patch Pipeline API</a>
   */
  @NonNull
  Pipeline updatePipeline(@NonNull String programId, @NonNull String pipelineId, @NonNull PipelineUpdate updates) throws CloudManagerApiException;

  /**
   * Changes details about a pipeline.
   *
   * @param pipeline the pipeline to update
   * @param updates  the updates to make to the pipeline
   * @return the updated pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipelines/patchPipeline">Patch Pipeline API</a>
   */
  @NonNull
  Pipeline updatePipeline(@NonNull Pipeline pipeline, @NonNull PipelineUpdate updates) throws CloudManagerApiException;

  // TODO: Finish these.
  void invalidatePipelineCache(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  void invalidatePipelineCache(@NonNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Returns an optional current execution of the specified pipeline.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id of to find the execution
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getCurrentExecution">Get Current Pipeline Execution API</a>
   */
  @NonNull
  Optional<PipelineExecution> getCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Returns an optional current execution of the specified pipeline.
   *
   * @param pipeline the pipeline reference
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getCurrentExecution">Get Current Pipeline Execution API</a>
   */
  @NonNull
  Optional<PipelineExecution> getCurrentExecution(@NonNull Pipeline pipeline) throws CloudManagerApiException;

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
  @NonNull
  PipelineExecution startExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

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
  @NonNull
  PipelineExecution startExecution(@NonNull Pipeline pipeline) throws CloudManagerApiException;

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
  @NonNull
  PipelineExecution getExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param pipeline    the pipeline context for the exectuion
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getExecution">Get Pipeline Execution API</a>
   */
  @NonNull
  PipelineExecution getExecution(@NonNull Pipeline pipeline, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the Pipeline Execution associated with the AdobeIO Execution Start event
   *
   * @param event the pipeline start event
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull
  PipelineExecution getExecution(@NonNull PipelineExecutionStartEvent event) throws CloudManagerApiException;

  /**
   * Returns the Pipeline Execution associated with the AdobeIO Execution End event
   *
   * @param event the pipeline end event
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NonNull
  PipelineExecution getExecution(@NonNull PipelineExecutionEndEvent event) throws CloudManagerApiException;

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
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  @NonNull
  PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException;

  /**
   * Gets the step state for execution based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  @NonNull
  PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepStartEvent event) throws CloudManagerApiException;

  /**
   * Gets the step state for execution based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  @NonNull
  PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepWaitingEvent event) throws CloudManagerApiException;

  /**
   * Gets the step state for execution based on the provided event.
   *
   * @param event the event context
   * @return the step state
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  @NonNull
  PipelineExecutionStepState getExecutionStepState(@NonNull PipelineExecutionStepEndEvent event) throws CloudManagerApiException;

  /**
   * Gets the current step for the execution.
   *
   * @param execution the pipeline execution
   * @return the current step
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  @NonNull
  PipelineExecutionStepState getCurrentStep(@NonNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Gets the waiting step for the execution.
   *
   * @param execution the pipeline execution
   * @return the waiting step
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepState">Get Step State API</a>
   */
  @NonNull
  PipelineExecutionStepState getWaitingStep(@NonNull PipelineExecution execution) throws CloudManagerApiException;
  
  /**
   * Advances the current execution of the specified pipeline. If no current execution exists, quietly does nothing.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline to cancel
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/advancePipelineExecution">Advance Pipeline API</a>
   */
  void advanceCurrentExecution(@NonNull String programId, @NonNull String pipelineId) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be advanced
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/advancePipelineExecution">Advance Pipeline API</a>
   */
  void advanceExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Advances the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be advanced
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/advancePipelineExecution">Advance Pipeline API</a>
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
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/cancelPipelineExecutionStep">Cancel Pipeline API</a>
   */
  void cancelExecution(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId) throws CloudManagerApiException;

  /**
   * Cancels the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be canceled
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/cancelPipelineExecutionStep">Cancel Pipeline API</a>
   */
  void cancelExecution(@NonNull PipelineExecution execution) throws CloudManagerApiException;
  
  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Adobe Cloud Manager API</a>
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs.
   */
  @NonNull
  String getExecutionStepLogDownloadUrl(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Adobe Cloud Manager API</a>
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @param name         custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs.
   */
  @NonNull
  String getExecutionStepLogDownloadUrl(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull String name) throws CloudManagerApiException;


  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Adobe Cloud Manager API</a>
   * @param execution    the execution for the log
   * @param action       the execution step action for the log
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs.
   */
  @NonNull
  String getExecutionStepLogDownloadUrl(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException;

  /**
   * Returns the fully qualified URL to the log file for download.
   *
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Adobe Cloud Manager API</a>
   * @param execution    the execution for the log
   * @param action       the execution step action for the log
   * @param name         custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs.
   */
  @NonNull
  String getExecutionStepLogDownloadUrl(@NonNull PipelineExecution execution, @NonNull String action, @NonNull String name) throws CloudManagerApiException;

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
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Get Execution Logs API</a>
   */
  void downloadExecutionStepLog(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param programId    the program id of the pipeline context
   * @param pipelineId   the pipeline id for the execution context
   * @param executionId  the execution id for the logs
   * @param action       the execution step action for the log
   * @param filename         custom log file name
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Get Execution Logs API</a>
   */
  void downloadExecutionStepLog(@NonNull String programId, @NonNull String pipelineId, @NonNull String executionId, @NonNull String action, @NonNull String filename, @NonNull OutputStream outputStream) throws CloudManagerApiException;

  /**
   * Streams the specified Execution Step log to the provided output stream. This will close the output stream when done.
   *
   * @param execution    the execution for the log
   * @param action       the step action for the log
   * @param outputStream output stream to write to
   * @throws CloudManagerApiException when any error occurs.
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Get Execution Logs API</a>
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
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/getStepLogs">Get Execution Logs API</a>
   */
  void downloadExecutionStepLog(@NonNull PipelineExecution execution, @NonNull String action, @NonNull String filename, @NonNull OutputStream outputStream) throws CloudManagerApiException;


  /**
   * Retrieves the metrics for the specified execution and step, if any.
   *
   * @param execution the execution step
   * @param action    the action step for which quality metrics are desired
   * @return the metrics for the execution
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Pipeline_Execution/stepMetric">Get Step Metrics API</a>
   */
  @NonNull
  Collection<Metric> getQualityGateResults(@NonNull PipelineExecution execution, @NonNull String action) throws CloudManagerApiException;

}

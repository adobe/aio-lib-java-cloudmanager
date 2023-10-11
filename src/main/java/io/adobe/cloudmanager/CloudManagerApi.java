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
import java.util.Collection;
import javax.validation.constraints.NotNull;

import io.adobe.cloudmanager.event.PipelineExecutionEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepWaitingEvent;

/**
 * API for interacting with Cloud Manager AdobeIO endpoints.
 * <p>
 * Implementations of this interface must be context aware of the Adobe IMS Org when making API calls.
 *
 * @see <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/">Cloud Manager API</a>
 * @since 1.0
 */
public interface CloudManagerApi {


  // Try to keep the APIs in the order they are listed on the Reference Docs
  // Helper APIs come after the associated publicly defined ones
  // Reference: https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/

  //TODO: Add the Execution Artifact API

  // Non-API convenience methods.

  // Below are still in work.

  /**
   * Returns the Pipeline Execution associated with the AdobeIO Execution Start event
   *
   * @param event the pipeline start event
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution getExecution(@NotNull PipelineExecutionStartEvent event) throws CloudManagerApiException;

  /**
   * Returns the Pipeline Execution associated with the AdobeIO Execution End event
   *
   * @param event the pipeline end event
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution getExecution(@NotNull PipelineExecutionEndEvent event) throws CloudManagerApiException;

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



}

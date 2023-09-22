package io.adobe.cloudmanager.impl;

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

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.EnvironmentLog;
import io.adobe.cloudmanager.LogOption;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import io.adobe.cloudmanager.event.PipelineExecutionEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepEndEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepStartEvent;
import io.adobe.cloudmanager.event.PipelineExecutionStepWaitingEvent;
import io.adobe.cloudmanager.impl.pipeline.execution.PipelineExecutionStepStateImpl;

public class CloudManagerApiImpl implements CloudManagerApi {
  static final String GENERATE_BODY = "Unable to generate request body: %s.";


  public CloudManagerApiImpl() {
  }


  // Non-API convenience methods.

  // Below are still in work.
  @Override
  public PipelineExecution getExecution(PipelineExecutionStartEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecution getExecution(PipelineExecutionEndEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public boolean isExecutionRunning(PipelineExecution execution) throws CloudManagerApiException {
    return false;
  }

  @Override
  public boolean isExecutionRunning(String programId, String pipelineId, String executionId) throws CloudManagerApiException {
    return false;
  }

  @Override
  public PipelineExecutionStepState getExecutionStepState(PipelineExecutionStepStartEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecutionStepState getExecutionStepState(PipelineExecutionStepWaitingEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecutionStepState getExecutionStepState(PipelineExecutionStepEndEvent event) throws CloudManagerApiException {
    return null;
  }

  @Override
  public PipelineExecutionStepStateImpl getCurrentStep(PipelineExecution execution) throws CloudManagerApiException {
    return null;
  }

  @Override
  public void advanceCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {

  }

  @Override
  public void cancelCurrentExecution(String programId, String pipelineId) throws CloudManagerApiException {

  }

  @Override
  public void downloadExecutionStepLog(String programId, String pipelineId, String executionId, String action, OutputStream outputStream) throws CloudManagerApiException {

  }

  @Override
  public void downloadExecutionStepLog(String programId, String pipelineId, String executionId, String action, String name, OutputStream outputStream) throws CloudManagerApiException {

  }

  @Override
  public void downloadExecutionStepLog(PipelineExecution execution, String action, OutputStream outputStream) throws CloudManagerApiException {

  }

  @Override
  public void downloadExecutionStepLog(PipelineExecution execution, String action, String filename, OutputStream outputStream) throws CloudManagerApiException {

  }

  @Override
  public Collection<EnvironmentLog> downloadLogs(String programId, String environmentId, LogOption logOption, int days, File dir) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Collection<EnvironmentLog> downloadLogs(Environment environment, LogOption logOption, int days, File dir) throws CloudManagerApiException {
    return null;
  }


  // Helper Methods

}

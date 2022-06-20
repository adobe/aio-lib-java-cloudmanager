package com.adobe.aio.cloudmanager.feign.exception;

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

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import feign.Response;

public class PipelineExecutionExceptionDecoder extends CloudManagerExceptionDecoder {

  private static final int BUSY = 412;
  
  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "PipelineExecutionApiClient#current(String,String)":       // Current 
      case "PipelineExecutionApiClient#get(String,String,String)":    // Specific
      case "PipelineExecutionApiClient#get(String)": {                // Path from Event
        type = ErrorType.GET_EXECUTION;
        break;
      }
      case "PipelineExecutionApiClient#start(String,String)": {
        if (status == BUSY) {
          type = ErrorType.PIPELINE_START_RUNNING;
        } else {
          type = ErrorType.PIPELINE_START;
        }
        break;
      }
      case "PipelineExecutionApiClient#getStepState(String)": {
        type = ErrorType.GET_STEP_STATE;
        break;
      }
      case "PipelineExecutionApiClient#advance(String,String)": {
        type = ErrorType.ADVANCE_EXECUTION;
        break;
      }
      case "PipelineExecutionApiClient#cancel(String,String)": {
        type = ErrorType.CANCEL_EXECUTION;
        break;
      }
      case "PipelineExecutionApiClient#getLogs(String,Map)": {
        type = ErrorType.GET_LOGS;
        break;
      }
      case "PipelineExecutionApiClient#getStepMetrics(String)": {
        type = ErrorType.GET_METRICS;
        break;
      }
      default: {
        type = ErrorType.UNKNOWN;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  public enum ErrorType {
    GET_EXECUTION("Cannot get execution: %s."),
    PIPELINE_START("Cannot create execution: %s."),
    PIPELINE_START_RUNNING("Cannot create execution. Pipeline already running."),
    FIND_STEP_STATE("Cannot find step state for action '%s' on execution %s."),
    FIND_EXECUTION_LINK("Cannot find execution link for the current step (%s)."),
    GET_STEP_STATE("Cannot get step state: %s."),
    FIND_CURRENT_STEP("Cannot find a current step for pipeline %s."),
    FIND_WAITING_STEP("Cannot find a waiting step for pipeline %s."),
    ADVANCE_EXECUTION("Cannot advance execution: %s."),
    CANCEL_EXECUTION("Cannot cancel execution: %s."),
    GET_LOGS("Cannot get logs: %s."),
    GET_METRICS("Cannot get metrics: %s."),
    UNKNOWN("Pipeline Execution API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}

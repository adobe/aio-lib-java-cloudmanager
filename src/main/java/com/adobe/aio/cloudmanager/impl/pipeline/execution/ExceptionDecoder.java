package com.adobe.aio.cloudmanager.impl.pipeline.execution;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
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
import com.adobe.aio.cloudmanager.exception.PipelineRunningException;
import com.adobe.aio.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.Getter;

public class ExceptionDecoder extends CloudManagerExceptionDecoder {

  private static final int NOT_FOUND = 404;
  private static final int BUSY = 412;


  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type = ErrorType.UNKNOWN;
    switch (methodKey) {
      case "FeignApi#current(String,String)": {      // Current
         if (status == NOT_FOUND) {
           return new CurrentNotFoundException("Current execution not found.");
         }
      } // Intentional fall through.
      case "FeignApi#get(String,String,String)":    // Specific
      case "FeignApi#get(String)": {                // via Event
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#start(String,String)": {
        if (status == BUSY) {
          return new PipelineRunningException("Cannot create execution. Pipeline already running.");
        } else {
          type = ErrorType.START;
        }
        break;
      }
      case "FeignApi#advance(String,String,String,String,String,String)": {
        type = ErrorType.ADVANCE;
        break;
      }
      case "FeignApi#cancel(String,String,String,String,String,String)": {
        type = ErrorType.CANCEL;
        break;
      }
      case "FeignApi#getLogs(String,String,String,String,String)":
      case "FeignApi#getLogs(String,String,String,String,String,String)": {
        type = ErrorType.GET_LOGS;
        break;
      }
      case "FeignApi#getStepMetrics(String,String,String,String,String)": {
        type = ErrorType.GET_METRICS;
        break;
      }
      case "FeignApi#list(String,String)":
      case "FeignApi#list(String,String,int,int)": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#listArtifacts(String,String,String,String,String)": {
        type = ErrorType.LIST_ARTIFACTS;
        break;
      }
      case "FeignApi#getArtifact(String,String,String,String,String,String)": {
        type = ErrorType.GET_ARTIFACT;
        break;
      }
      case "FeignApi#getStepState(String)": {   // via Event
        type = ErrorType.GET_STEP_STATE;
        break;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)));
  }

  @Getter
  private enum ErrorType {
    GET("Cannot get execution: %s."),
    START("Cannot create execution: %s."),
    ADVANCE("Cannot advance execution: %s."),
    CANCEL("Cannot cancel execution: %s."),
    GET_LOGS("Cannot get logs: %s."),
    GET_METRICS("Cannot get metrics: %s."),
    LIST("Cannot list executions: %s."),
    LIST_ARTIFACTS("Cannot list step artifacts: %s."),
    GET_ARTIFACT("Cannot get step artifact: %s."),
    GET_STEP_STATE("Cannot get execution step state: %s."),
    UNKNOWN("Pipeline Execution API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

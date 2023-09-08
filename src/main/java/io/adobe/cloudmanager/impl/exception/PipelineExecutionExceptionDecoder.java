package io.adobe.cloudmanager.impl.exception;

import feign.Response;
import io.adobe.cloudmanager.CloudManagerApiException;
import lombok.Getter;

public class PipelineExecutionExceptionDecoder extends CloudManagerExceptionDecoder {

  private static final int BUSY = 412;

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "FeignPipelineExecutionApi#current(String,String)":       // Current
      case "FeignPipelineExecutionApi#get(String,String,String)": {  // Specific
        type = ErrorType.GET_EXECUTION;
        break;
      }
      case "FeignPipelineExecutionApi#start(String,String)": {
        if (status == BUSY) {
          type = ErrorType.PIPELINE_START_RUNNING;
        } else {
          type = ErrorType.PIPELINE_START;
        }
        break;
      }
      case "FeignPipelineExecutionApi#advance(String,String,String,String,String,String)": {
        type = ErrorType.ADVANCE_EXECUTION;
        break;
      }
      case "FeignPipelineExecutionApi#cancel(String,String,String,String,String,String)": {
        type = ErrorType.CANCEL_EXECUTION;
        break;
      }
      case "FeignPipelineExecutionApi#getLogs(String,String,String,String,String)":
      case "FeignPipelineExecutionApi#getLogs(String,String,String,String,String,String)": {
        type = ErrorType.GET_LOGS;
        break;
      }
      case "FeignPipelineExecutionApi#getStepMetrics(String,String,String,String,String)": {
        type = ErrorType.GET_METRICS;
        break;
      }
      case "FeignPipelineExecutionApi#list(String,String)":
      case "FeignPipelineExecutionApi#list(String,String,int,int)": {
        type = ErrorType.LIST_EXECUTIONS;
        break;
      }
      default: {
        type = ErrorType.UNKNOWN;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  @Getter
  public enum ErrorType {
    GET_EXECUTION("Cannot get execution: %s."),
    PIPELINE_START("Cannot create execution: %s."),
    PIPELINE_START_RUNNING("Cannot create execution. Pipeline already running."),
    FIND_STEP_STATE("Cannot find step state for action '%s' on execution %s."),
    ADVANCE_EXECUTION("Cannot advance execution: %s."),
    CANCEL_EXECUTION("Cannot cancel execution: %s."),
    GET_LOGS("Cannot get logs: %s."),
    GET_METRICS("Cannot get metrics: %s."),
    LIST_EXECUTIONS("Cannot list executions: %s."),
//    FIND_CURRENT_STEP("Cannot find a current step for pipeline %s."),
    UNKNOWN("Pipeline Execution API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

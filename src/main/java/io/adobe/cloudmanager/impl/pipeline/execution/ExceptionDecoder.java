package io.adobe.cloudmanager.impl.pipeline.execution;

import feign.Response;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.Getter;

public class ExceptionDecoder extends CloudManagerExceptionDecoder {

  private static final int BUSY = 412;

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "FeignApi#current(String,String)":       // Current
      case "FeignApi#get(String,String,String)": {  // Specific
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#start(String,String)": {
        if (status == BUSY) {
          type = ErrorType.ALREADY_RUNNING;
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
      default: {
        type = ErrorType.UNKNOWN;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  @Getter
  public enum ErrorType {
    GET("Cannot get execution: %s."),
    START("Cannot create execution: %s."),
    ALREADY_RUNNING("Cannot create execution. Pipeline already running."),
    FIND_STEP_STATE("Cannot find step state for action '%s' on execution %s."),
    ADVANCE("Cannot advance execution: %s."),
    CANCEL("Cannot cancel execution: %s."),
    GET_LOGS("Cannot get logs: %s."),
    GET_METRICS("Cannot get metrics: %s."),
    LIST("Cannot list executions: %s."),
    LIST_ARTIFACTS("Cannot list step artifacts: %s."),
    GET_ARTIFACT("Cannot get step artifact: %s."),
    UNKNOWN("Pipeline Execution API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

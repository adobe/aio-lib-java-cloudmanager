package io.adobe.cloudmanager.impl.exception;

import feign.Response;
import io.adobe.cloudmanager.CloudManagerApiException;

public class PipelineExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "FeignPipelineApi#list(String)": {
        type = ErrorType.LIST_PIPELINES;
        break;
      }
      case "FeignPipelineApi#get(String,String)": {
        type = ErrorType.GET_PIPELINE;
        break;
      }
      case "FeignPipelineApi#delete(String,String)":{
        type = ErrorType.DELETE_PIPELINE;
        break;
      }
      case "FeignPipelineApi#update(String,String,Pipeline)": {
        type = ErrorType.UPDATE_PIPELINE;
        break;
      }
      case "FeignPipelineApi#invalidateCache(String,String)": {
        type = ErrorType.INVALIDATE_CACHE;
        break;
      }
      default: {
        type = ErrorType.UNKNOWN;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  public enum ErrorType {
    LIST_PIPELINES("Cannot retrieve pipelines: %s."),
    FIND_PIPELINES("Could not find pipelines for program %s."),
    GET_PIPELINE("Cannot retrieve pipeline: %s."),
    DELETE_PIPELINE("Cannot delete pipeline: %s."),
    UPDATE_PIPELINE("Cannot update pipeline: %s."),
    NO_BUILD_PHASE("Pipeline %s does not appear to have a build phase."),
    INVALIDATE_CACHE("Cannot invalidate pipeline cache: %s."),
    UNKNOWN("Pipeline API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}

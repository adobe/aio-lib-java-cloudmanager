package io.adobe.cloudmanager.impl.pipeline;

import feign.Response;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.Getter;

public class ExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "FeignApi#list(String)": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#delete(String,String)":{
        type = ErrorType.DELETE;
        break;
      }
      case "FeignApi#update(String,String,Pipeline)": {
        type = ErrorType.UPDATE;
        break;
      }
      case "FeignApi#invalidateCache(String,String)": {
        type = ErrorType.INVALIDATE_CACHE;
        break;
      }
      case "FeignApi#getVariables(String,String)": {
        type = ErrorType.LIST_VARIABLES;
        break;
      }
      case "FeignApi#setVariables(String,String,List)": {
        type = ErrorType.SET_VARIABLES;
        break;
      }
      default: {
        type = ErrorType.UNKNOWN;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  @Getter
  private enum ErrorType {
    LIST("Cannot retrieve pipelines: %s."),
    GET("Cannot retrieve pipeline: %s."),
    DELETE("Cannot delete pipeline: %s."),
    UPDATE("Cannot update pipeline: %s."),
    INVALIDATE_CACHE("Cannot invalidate pipeline cache: %s."),
    LIST_VARIABLES("Cannot list pipeline variables: %s."),
    SET_VARIABLES("Cannot set pipeline variables: %s."),
    UNKNOWN("Pipeline API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

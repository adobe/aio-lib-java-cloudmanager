package io.adobe.cloudmanager.impl.environment;

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
      case "FeignApi#list(String)":
      case "FeignApi#list(String,String)": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#create(String,String)": {
        type = ErrorType.CREATE;
        break;
      }
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#delete(String,String,boolean)": {
        if (status == 400) {
          type = ErrorType.DELETE_IN_PROGRESS;
        } else {
          type = ErrorType.DELETE;
        }
        break;
      }
      case "FeignApi#listLogs(String,String,String,String,int)":
      case "FeignApi#getLogs(String,String,String,String,String)": {
        type = ErrorType.GET_LOGS;
        break;
      }
      case "FeignApi#getDeployment(String,String,String)": {
        type = ErrorType.GET_DEPLOYMENT;
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
  public enum ErrorType {
    LIST("Cannot list environments: %s."),
    CREATE("Cannot create environment: %s."),
    GET("Cannot get environment: %s"),
    DELETE("Cannot delete environment: %s."),
    DELETE_IN_PROGRESS("Cannot delete environment, deletion in progress."),
    GET_LOGS("Cannot get logs: %s."),
    GET_DEPLOYMENT("Cannot get region deployment: %s."),
    LIST_VARIABLES("Cannot list environment variables: %s."),
    SET_VARIABLES("Cannot set environment variables: %s."),
    UNKNOWN("Environment API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}

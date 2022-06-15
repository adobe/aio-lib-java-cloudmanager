package com.adobe.aio.cloudmanager.feign.exception;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import feign.Response;

public class EnvironmentExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "EnvironmentApiClient#list(String,Map)" : {
        type = ErrorType.RETRIEVE_ENVIRONMENTS;
        break;
      }
      case "EnvironmentApiClient#delete(String,String)": {
        type = ErrorType.DELETE_ENVIRONMENT;
        break;
      }
      case "EnvironmentApiClient#listLogs(String,String,Map)":
      case "EnvironmentApiClient#downloadLog(String,String,Map)": {
        type = ErrorType.GET_LOGS;
        break;
      }
      default: {
        type = ErrorType.UNKNOWN;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  public enum ErrorType {
    RETRIEVE_ENVIRONMENTS("Could not find environments: %s."),
    FIND_ENVIRONMENT("Could not find environment %s for program %s."),
    DELETE_ENVIRONMENT("Cannot delete environment: %s"),
    GET_LOGS("Cannot get logs: %s"),
    UNKNOWN("Environment API Error: %s");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}

package io.adobe.cloudmanager.impl.repository;

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
      case "FeignApi#list(String,int,int)":{
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#listBranches(String,String)": {
        type = ErrorType.BRANCHES;
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
    LIST("Cannot retrieve repositories: %s."),
    GET("Cannot retrieve repository: %s."),
    BRANCHES("Cannot retrieve repository branches: %s."),
    UNKNOWN("Repository API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

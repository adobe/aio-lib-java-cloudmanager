package io.adobe.cloudmanager.impl.exception;

import feign.Response;
import io.adobe.cloudmanager.CloudManagerApiException;
import lombok.Getter;

public class RepositoryExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "FeignRepositoryApi#list(String)":
      case "FeignRepositoryApi#list(String,int,int)":{
        type = ErrorType.LIST_REPOSITORIES;
        break;
      }
      case "FeignRepositoryApi#get(String,String)": {
        type = ErrorType.GET_REPOSITORY;
        break;
      }
      case "FeignRepositoryApi#listBranches(String,String)": {
        type = ErrorType.LIST_BRANCHES;
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
    LIST_REPOSITORIES("Cannot retrieve repositories: %s."),
    GET_REPOSITORY("Cannot retrieve repository: %s."),
    LIST_BRANCHES("Cannot retrieve repository branches: %s."),
    UNKNOWN("Repository API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

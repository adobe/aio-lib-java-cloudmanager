package io.adobe.cloudmanager.impl.network.ipallow;

import feign.Response;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.Getter;

public class ExceptionDecoder extends CloudManagerExceptionDecoder {
  private static final int NOT_CS = 412;

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch(methodKey) {
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#update(String,String,IPAllowedList)": {
        type = ErrorType.UPDATE;
        break;
      }
      case "FeignApi#delete(String,String)": {
        type = ErrorType.DELETE;
        break;
      }
      case "FeignApi#list(String)": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#create(String,IPAllowedList)": {
        if (status == NOT_CS) {
          type = ErrorType.CREATE_NOT_CS;
        } else {
          type = ErrorType.CREATE;
        }
        break;
      }
      case "FeignApi#getBinding(String,String,String)": {
        type = ErrorType.GET_BINDING;
        break;
      }
      case "FeignApi#deleteBinding(String,String,String)": {
        type = ErrorType.DELETE_BINDING;
        break;
      }
      case "FeignApi#retryBinding(String,String,String)": {
        type = ErrorType.RETRY_BINDING;
        break;
      }
      case "FeignApi#listBindings(String,String)": {
        type = ErrorType.LIST_BINDINGS;
        break;
      }
      case "FeignApi#createBinding(String,String,IPAllowedListBinding)": {
        if (status == NOT_CS) {
          type = ErrorType.CREATE_NOT_CS;
        } else {
          type = ErrorType.CREATE_BINDING;
        }
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
    GET("Cannot get IP Allow List: %s."),
    UPDATE("Cannot update IP Allow List: %s."),
    DELETE("Cannot delete IP Allow List: %s."),
    LIST("Cannot list IP Allow Lists: %s."),
    CREATE("Cannot create IP Allow List: %s."),
    CREATE_NOT_CS("IP Allow Lists are not supported on non-Cloud Service programs."),
    GET_BINDING("Cannot get IP Allow List binding: %s."),
    DELETE_BINDING("Cannot delete IP Allow List binding: %s."),
    RETRY_BINDING("Cannot rebind IP Allow List binding: %s."),
    LIST_BINDINGS("Cannot list IP Allow List bindings: %s."),
    CREATE_BINDING("Cannot create IP Allow List binding: %s."),
    UNKNOWN("IP Allow List API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}

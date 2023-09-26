package io.adobe.cloudmanager.impl.network.dns;

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
      case "FeignApi#list(String)":
      case "FeignApi#list(String,int,int)": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#create(String,NewDomainName)": {
        if (status == NOT_CS) {
          type = ErrorType.CREATE_NOT_CS;
        } else {
          type = ErrorType.CREATE;
        }
        break;
      }
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#update(String,String,DomainName)": {
        type = ErrorType.UPDATE;
        break;
      }
      case "FeignApi#delete(String,String)": {
        type = ErrorType.DELETE;
        break;
      }
      case "FeignApi#deploy(String,String)": {
        type = ErrorType.DEPLOY;
        break;
      }
      case "FeignApi#verify(String,String)": {
        type = ErrorType.VERIFY;
        break;
      }
      case "FeignApi#validate(String,String,int,int)": {
        type = ErrorType.VALIDATE;
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
    LIST("Cannot list Domain Names: %s."),
    CREATE("Cannot create Domain Name: %s."),
    CREATE_NOT_CS("Domain Names are not supported on non-Cloud Service programs."),
    GET("Cannot get Domain Name: %s."),
    UPDATE("Cannot update Domain Name: %s."),
    DELETE("Cannot delete Domain Name: %s."),
    DEPLOY("Cannot deploy Domain Name: %s."),
    VERIFY("Cannot verify Domain Name: %s."),
    VALIDATE("Cannot validate Domain Name: %s."),
    UNKNOWN("Domain Name API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}

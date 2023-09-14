package io.adobe.cloudmanager.impl.tenant;

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
      case "FeignApi#list()": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#get(String)": {
        type = ErrorType.GET;
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
    LIST("Cannot retrieve tenants: %s."),
    GET("Cannot retrieve tenant: %s."),
    UNKNOWN("Tenant API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}

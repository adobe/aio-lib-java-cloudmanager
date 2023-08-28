package io.adobe.cloudmanager.impl.exception;

import feign.Response;
import io.adobe.cloudmanager.CloudManagerApiException;
import lombok.Getter;

public class TenantExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "FeignTenantApi#list()": {
        type = ErrorType.LIST_TENANTS;
        break;
      }
      case "FeignTenantApi#get(String)": {
        type = ErrorType.GET_TENANT;
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
    LIST_TENANTS("Cannot retrieve tenants: %s."),
    GET_TENANT("Cannot retrieve tenant: %s."),
    UNKNOWN("Tenant API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}

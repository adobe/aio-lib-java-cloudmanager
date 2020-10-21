package io.adobe.cloudmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.impl.ConfiguredApiClient;
import io.adobe.cloudmanager.swagger.invoker.ApiException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class CloudManagerApiException extends Exception {

  private static final String DEFAULT_REASON = "Unknown";

  private final String message;

  public CloudManagerApiException(ErrorType type, String baseUrl, String apiPath, ApiException cause) {
    super(cause);
    ErrorPayload errorBody = getErrorBody(cause);

    StringBuilder errorBuilder = new StringBuilder();
    errorBuilder.append(baseUrl).append(apiPath).append(' ').append(String.format("(%s %s)", cause.getCode(), getReason(cause)));

    if (errorBody != null) {
      String errorMessage = errorBody.errorMessage;
      if (errorMessage != null) {
        errorBuilder.append(" - Detail: ").append(errorMessage);
        String errorCode = errorBody.errorCode;
        if (errorCode != null) {
          errorBuilder.append(" (Code: ").append(errorCode).append(")");
        }
      }
    }

    this.message = String.format(type.message, errorBuilder.toString());
  }

  public CloudManagerApiException(ErrorType type, String... vars) {
    this.message = String.format(type.message, (Object[]) vars);
  }

  @Override
  public String getMessage() {
    return message;
  }

  private static ErrorPayload getErrorBody(ApiException cause) {
    String contentType = getHeader(cause, HttpHeaders.CONTENT_TYPE, null);
    if (contentType != null) {
      MediaType parsed = MediaType.valueOf(contentType);
      if (MediaType.APPLICATION_JSON_TYPE.isCompatible(parsed)) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
          return objectMapper.readValue(cause.getResponseBody(), ErrorPayload.class);
        } catch (JsonProcessingException e) {
          // TODO -- log?
          return null;
        }
      }
    }
    return null;
  }

  private static String getReason(ApiException cause) {
    return getHeader(cause, ConfiguredApiClient.HEADER_REASON, DEFAULT_REASON);
  }

  private static String getHeader(ApiException cause, String headerName, String defaultValue) {
    if (cause == null) {
      return defaultValue;
    }
    List<String> header = cause.getResponseHeaders().get(headerName.toLowerCase());
    if (header == null || header.isEmpty()) {
      return defaultValue;
    }
    return header.get(0);
  }

  public enum ErrorType {
    LIST_PROGRAMS("Cannot retrieve programs: %s"),
    GET_PROGRAM("Cannot retrieve program: %s"),
    FIND_PROGRAM("Could not find program %s");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }

  private static class ErrorPayload {

    private String errorCode;
    private String errorMessage;

    @JsonProperty("error_code")
    public void setErrorCode(String errorCode) {
      this.errorCode = errorCode;
    }

    @JsonProperty("message")
    public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }
}

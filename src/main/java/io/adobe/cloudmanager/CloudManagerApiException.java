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
    LIST_PIPELINES("Cannot retrieve pipelines: %s"),
    FIND_PROGRAM("Could not find program %s"),
    FIND_PIPELINES("Could not find pipelines for program %s"),
    FIND_PIPELINE_START("Cannot start execution. Pipeline %s does not exist in program %s."),
    FIND_PIPELINE("Pipeline %s does not exist in program %s."),
    PIPELINE_START("Cannot create execution: %s"),
    PIPELINE_START_RUNNING("Cannot create execution. Pipeline already running."),
    GET_EXECUTION("Cannot get execution: %s"),
    FIND_STEP_STATE("Cannot find step state for action %s on execution %s."),
    GET_METRICS("Cannot get metrics: %s."),
    FIND_CURRENT_STEP("Cannot find a current step for pipeline %s."),
    FIND_CANCEL_LINK("Cannot find a cancel link for the current step (%s). Step may not be cancellable."),
    FIND_ADVANCE_LINK("Cannot find an advance link for the current step (%s)."),
    CANCEL_EXECUTION("Cannot cancel execution: %s"),
    ADVANCE_EXECUTION("Cannot advance execution: %s"),
    FIND_WAITING_STEP("Cannot find a waiting step for pipeline %s."),
    FIND_ENVIRONMENTS("Could not find environments for program %s."),
    FIND_ENVIRONMENT("Could not find environment %s for program %s."),
    RETRIEVE_ENVIRONMENTS("Could not find environments for program %s."),
    DELETE_PIPELINE("Cannot delete pipeline: %s"),
    GET_LOG("Cannot get log: %s"),
    NO_LOG_REDIRECT("Log %s did not contain a redirect. Was %s."),
    LOG_DOWNLOAD("Could not download %s to %s (%s %s)."),
    LOG_UNZIP("Could not unzip %s to %s."),
    LOG_INITIAL_SIZE("Could not get initial size of %s"),
    FIND_LOG("Log not found: %s (%s %s)"),
    TAIL_LOG("Cannot tail log: %s (%s %s)"),
    FIND_TAIL_LOGS("No logs for tailing available in %s for program %s"),
    FIND_LOGS("No logs available in %s for program %s"),
    NO_BUILD_PHASE("Pipeline %s does not appear to have a build phase."),
    NO_DEVELOPER_CONSOLE("Environment %s does not appear to support Developer Console."),
    FIND_VARIABLES_LINK_ENVIRONMENT("Could not find variables link for environment %s for program %s."),
    FIND_VARIABLES_LINK_PIPELINE("Could not find variables link for pipeline %s for program %s."),
    GET_VARIABLES("Cannot get variables: %s"),
    SET_VARIABLES("Cannot set variables: %s"),
    DELETE_PROGRAM("Cannot delete program: %s"),
    DELETE_ENVIRONMENT("Cannot delete environment: %s"),
    UPDATE_PIPELINE("Cannot update pipeline: %s");

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

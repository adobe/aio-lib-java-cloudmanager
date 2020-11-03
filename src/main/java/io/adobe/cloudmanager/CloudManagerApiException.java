package io.adobe.cloudmanager;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 Adobe Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.impl.ConfiguredApiClient;
import io.adobe.cloudmanager.swagger.invoker.ApiException;

/**
 * Represents exception states that may occur during interactions with the AdobeIO API.
 */
public class CloudManagerApiException extends Exception {

  private static final String DEFAULT_REASON = "Unknown";

  private final String message;

  /**
   * Creates a new exception.
   *
   * @param type    the Error type that occurred.
   * @param baseUrl the base url for the AdobeIO endpoint that generated the error
   * @param apiPath the API path of the AdobeIO endpoint that generated the error
   * @param cause   the root cause of the error
   */
  public CloudManagerApiException(ErrorType type, String baseUrl, String apiPath, ApiException cause) {
    super(cause);

    ProblemPayload problemBody = getProblemBody(cause);
    ErrorPayload errorBody = getErrorBody(cause);

    StringBuilder errorBuilder = new StringBuilder();
    errorBuilder.append(baseUrl).append(apiPath).append(' ').append(String.format("(%s %s)", cause.getCode(), getReason(cause)));

    if (problemBody != null) {
      String errorMessage = String.join(", ", problemBody.errors);
      if ("http://ns.adobe.com/adobecloud/validation-exception".equals(problemBody.type)) {
        errorBuilder.append(" - Validation Error(s): ").append(errorMessage);
      }
    } else if (errorBody != null) {
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

  /**
   * Generates a new exception with the specified message for the Error Type context.
   *
   * @param type the {@link ErrorType} that occurred
   * @param vars the custom message for the type context
   */
  public CloudManagerApiException(ErrorType type, String... vars) {
    this.message = String.format(type.message, (Object[]) vars);
  }

  private static ProblemPayload getProblemBody(ApiException cause) {
    String contentType = getHeader(cause, HttpHeaders.CONTENT_TYPE, null);
    if (StringUtils.equals(contentType, "application/problem+json")) {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        return objectMapper.readValue(cause.getResponseBody(), ProblemPayload.class);
      } catch (JsonProcessingException e) {
        return null;
      }
    }
    return null;
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

  @Override
  public String getMessage() {
    return message;
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
    RETRIEVE_ENVIRONMENTS("Could not find environments: %s."),
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
    UPDATE_PIPELINE("Cannot update pipeline: %s"),
    GENERATE_BODY("Unable to generate request body: %s");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }

  private static class ProblemPayload {

    private String type;
    private String[] errors;

    @JsonProperty("type")
    public void setErrorCode(String type) {
      this.type = type;
    }

    @JsonProperty("errors") public void setErrorMessage(String[] errors) { this.errors = errors; }
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

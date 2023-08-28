package io.adobe.cloudmanager.impl.exception;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;

public abstract class CloudManagerExceptionDecoder implements ErrorDecoder {

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String APPLICATION_JSON_TYPE = "application/json";

  private static final String DEFAULT_REASON = "Unknown";

  private static final Map<Integer, String> phrases = new HashMap<>();

  static {
    phrases.put(400, "Bad Request");
    phrases.put(403, "Forbidden");
    phrases.put(404, "Not Found");
    phrases.put(405, "Method Not Allowed");
  }

  private static ProblemPayload getProblemBody(Response response, String body) {
    String contentType = getHeader(response, CONTENT_TYPE, null);
    if (StringUtils.equals(contentType, "application/problem+json")) {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        return objectMapper.readValue(body, ProblemPayload.class);
      } catch (IOException e) {
        return null;
      }
    }
    return null;
  }

  private static String getHeader(Response response, String name, String defaultValue) {
    Collection<String> headers = response.headers().getOrDefault(name, null);
    if (headers == null || headers.isEmpty()) {
      return defaultValue;
    }
    return headers.stream().findFirst().orElse(defaultValue);
  }

  private static ErrorPayload getErrorBody(Response response, String body) {
    String contentType = getHeader(response, CONTENT_TYPE, null);
    if (contentType != null) {
      if (contentType.contains(APPLICATION_JSON_TYPE)) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
          return objectMapper.readValue(body, ErrorPayload.class);
        } catch (IOException e) {
          // TODO -- log?
          return null;
        }
      }
    }
    return null;
  }

  protected String getError(Response response) {
    int status = response.status();
    StringBuilder errorBuilder = new StringBuilder();
    errorBuilder.append(String.format("%s (%d %s)", response.request().url(), status, getReason(status)));
    
    Response.Body body = response.body();
    if (body != null) {
      try (InputStream is = body.asInputStream()) {
        String bodyAsString = IOUtils.toString(is, Charset.defaultCharset());
        processProblemBody(errorBuilder, response, bodyAsString);
        processErrorBody(errorBuilder, response, bodyAsString);
      } catch (IOException e) {
        // Do nothing?
      }
    }
    return errorBuilder.toString();
  }
  
  private void processProblemBody(StringBuilder builder, Response response, String body) {
    ProblemPayload problemBody = getProblemBody(response, body);
    if (problemBody != null) {
      String errorMessage = String.join(", ", problemBody.errors);
      if ("http://ns.adobe.com/adobecloud/validation-exception".equals(problemBody.type)) {
        builder.append(" - Validation Error(s): ").append(errorMessage);
      }
    }
  }
  
  private void processErrorBody(StringBuilder builder, Response response, String body) {
    ErrorPayload errorBody = getErrorBody(response, body);
    if (errorBody != null) {
      String errorMessage = errorBody.errorMessage;
      if (errorMessage != null) {
        builder.append(" - Detail: ").append(errorMessage);
        String errorCode = errorBody.errorCode;
        if (errorCode != null) {
          builder.append(" (Code: ").append(errorCode).append(")");
        }
      }
    }
  }

  protected static String getReason(int status) {
    return phrases.getOrDefault(status, DEFAULT_REASON);
  }

  private static class ProblemPayload {

    private String type;
    private String[] errors;

    @JsonProperty("type")
    public void setErrorCode(String type) {
      this.type = type;
    }

    @JsonProperty("errors")
    public void setErrorMessage(String[] errors) {
      this.errors = errors;
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

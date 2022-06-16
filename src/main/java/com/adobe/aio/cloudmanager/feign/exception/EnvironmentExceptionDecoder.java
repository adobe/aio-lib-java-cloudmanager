package com.adobe.aio.cloudmanager.feign.exception;

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

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import feign.Response;

public class EnvironmentExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "EnvironmentApiClient#list(String,Map)" : {
        type = ErrorType.RETRIEVE_ENVIRONMENTS;
        break;
      }
      case "EnvironmentApiClient#delete(String,String)": {
        type = ErrorType.DELETE_ENVIRONMENT;
        break;
      }
      case "EnvironmentApiClient#listLogs(String,String,Map)":
      case "EnvironmentApiClient#downloadLog(String,String,Map)": {
        type = ErrorType.GET_LOGS;
        break;
      }
      default: {
        type = ErrorType.UNKNOWN;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  public enum ErrorType {
    RETRIEVE_ENVIRONMENTS("Could not find environments: %s."),
    FIND_ENVIRONMENT("Could not find environment %s for program %s."),
    DELETE_ENVIRONMENT("Cannot delete environment: %s."),
    GET_LOGS("Cannot get logs: %s."),
    UNKNOWN("Environment API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}

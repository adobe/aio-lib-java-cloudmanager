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

import feign.Response;
import io.adobe.cloudmanager.CloudManagerApiException;
import lombok.Getter;

public class ProgramExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "FeignProgramApi#list()":
      case "FeignProgramApi#list(String)":{
        type = ErrorType.LIST_PROGRAMS;
        break;
      }
      case "FeignProgramApi#get(String)": {
        type = ErrorType.GET_PROGRAM;
        break;
      }
      case "FeignProgramApi#delete(String)": {
        type = ErrorType.DELETE_PROGRAM;
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
    LIST_PROGRAMS("Cannot retrieve programs: %s."),
    GET_PROGRAM("Cannot retrieve program: %s."),
    DELETE_PROGRAM("Cannot delete program: %s."),
    UNKNOWN("Program API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

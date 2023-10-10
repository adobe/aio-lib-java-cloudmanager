package io.adobe.cloudmanager.impl.program;

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
import io.adobe.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.Getter;

public class ExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "FeignApi#list(String)":{
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#get(String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#delete(String)": {
        type = ErrorType.DELETE;
        break;
      }
      case "FeignApi#listRegions(String)": {
        type = ErrorType.REGIONS;
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
    LIST("Cannot retrieve programs: %s."),
    GET("Cannot retrieve program: %s."),
    DELETE("Cannot delete program: %s."),
    REGIONS("Cannot retrieve program regions: %s."),
    UNKNOWN("Program API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}

package io.adobe.cloudmanager.impl.repository;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
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
    ErrorType type = ErrorType.UNKNOWN;
    switch (methodKey) {
      case "FeignApi#list(String)":
      case "FeignApi#list(String,int,int)":{
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#listBranches(String,String)": {
        type = ErrorType.BRANCHES;
        break;
      }
    }

    return new CloudManagerApiException(String.format(type.message, getError(response)));
  }

  @Getter
  private enum ErrorType {
    LIST("Cannot retrieve repositories: %s."),
    GET("Cannot retrieve repository: %s."),
    BRANCHES("Cannot retrieve repository branches: %s."),
    UNKNOWN("Repository API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

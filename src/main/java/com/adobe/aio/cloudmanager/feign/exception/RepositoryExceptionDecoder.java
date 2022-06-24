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

public class RepositoryExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "RepositoryApiClient#list(String)":
      case "RepositoryApiClient#list(String,Map)":{
        type = ErrorType.LIST_REPOSITORIES;
        break;
      }
      case "RepositoryApiClient#get(String,String)": {
        type = ErrorType.GET_REPOSITORY;
        break;
      }
      case "RepositoryApiClient#listBranches(String,String)": {
        type = ErrorType.LIST_BRANCHES;
        break;
      }
      default: {
        type = ErrorType.UNKNOWN;
      }
    }

    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  public enum ErrorType {
    LIST_REPOSITORIES("Cannot retrieve repositories: %s."),
    GET_REPOSITORY("Cannot retrieve repository: %s."),
    LIST_BRANCHES("Cannot retrieve repository branches: %s."),
    UNKNOWN("Repository API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

  }
}

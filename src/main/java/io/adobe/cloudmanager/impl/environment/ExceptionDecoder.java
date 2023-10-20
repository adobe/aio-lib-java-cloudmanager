package io.adobe.cloudmanager.impl.environment;

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
import io.adobe.cloudmanager.exception.DeleteInProgressException;
import io.adobe.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.Getter;

public class ExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type = ErrorType.UNKNOWN;
    switch (methodKey) {
      case "FeignApi#list(String)":
      case "FeignApi#list(String,String)": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#create(String,String)": {
        type = ErrorType.CREATE;
        break;
      }
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#delete(String,String,boolean)": {
        if (status == 400) {
          return new DeleteInProgressException("Cannot delete environment, deletion in progress.");
        } else {
          type = ErrorType.DELETE;
        }
        break;
      }
      case "FeignApi#listLogs(String,String,String,String,int)":
      case "FeignApi#getLogs(String,String,String,String,String)": {
        type = ErrorType.GET_LOGS;
        break;
      }
      case "FeignApi#getDeployment(String,String,String)": {
        type = ErrorType.GET_DEPLOYMENT;
        break;
      }
      case "FeignApi#listDeployments(String,String)": {
        type = ErrorType.LIST_DEPLOYMENTS;
        break;
      }
      case "FeignApi#addDeployments(String,String,List)": {
        type = ErrorType.CREATE_DEPLOYMENTS;
        break;
      }
      case "FeignApi#removeDeployments(String,String,List)": {
        type = ErrorType.REMOVE_DEPLOYMENTS;
        break;
      }
      case "FeignApi#getVariables(String,String)": {
        type = ErrorType.LIST_VARIABLES;
        break;
      }
      case "FeignApi#setVariables(String,String,List)": {
        type = ErrorType.SET_VARIABLES;
        break;
      }
      case "FeignApi#reset(String,String)": {
        type = ErrorType.RESET;
        break;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)));
  }

  @Getter
  private enum ErrorType {
    LIST("Cannot list environments: %s."),
    CREATE("Cannot create environment: %s."),
    GET("Cannot get environment: %s"),
    DELETE("Cannot delete environment: %s."),
    GET_LOGS("Cannot get logs: %s."),
    GET_DEPLOYMENT("Cannot get region deployment: %s."),
    LIST_DEPLOYMENTS("Cannot list region deployments: %s."),
    CREATE_DEPLOYMENTS("Cannot add region deployments: %s."),
    REMOVE_DEPLOYMENTS("Cannot remove region deployments: %s."),
    LIST_VARIABLES("Cannot list environment variables: %s."),
    SET_VARIABLES("Cannot set environment variables: %s."),
    RESET("Cannot reset rapid development environment: %s."),
    UNKNOWN("Environment API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}

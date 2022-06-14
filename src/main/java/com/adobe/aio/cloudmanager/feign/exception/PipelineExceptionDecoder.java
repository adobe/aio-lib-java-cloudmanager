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

public class PipelineExceptionDecoder extends CloudManagerExceptionDecoder {
  
  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    ErrorType type;
    switch (methodKey) {
      case "PipelineApiClient#list(String)": {
        type = ErrorType.LIST_PIPELINES;
        break;
      }
      case "PipelineApiClient#get(String,String)": {
        type = ErrorType.GET_PIPELINE;
        break;
      }
      case "PipelineApiClient#delete(String,String)":{
        type = ErrorType.DELETE_PIPELINE;
        break;
      }
      case "PipelineApiClient#update(String,String,Pipeline)": {
        type = ErrorType.UPDATE_PIPELINE;
        break;
      }
      default: {
        type = ErrorType.UNKNOWN;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  public enum ErrorType {
    LIST_PIPELINES("Cannot retrieve pipelines: %s"),
    FIND_PIPELINES("Could not find pipelines for program %s"),
    GET_PIPELINE("Cannot retrieve pipeline: %s"),
    DELETE_PIPELINE("Cannot delete pipeline: %s"),
    UPDATE_PIPELINE("Cannot update pipeline: %s"),
    NO_BUILD_PHASE("Pipeline %s does not appear to have a build phase."),
    UNKNOWN("Pipeline API Error: %s");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}

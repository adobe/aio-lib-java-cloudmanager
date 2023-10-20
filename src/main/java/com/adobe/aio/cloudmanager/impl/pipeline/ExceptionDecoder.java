package com.adobe.aio.cloudmanager.impl.pipeline;

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
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.Getter;

public class ExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    ErrorType type = ErrorType.UNKNOWN;
    switch (methodKey) {
      case "FeignApi#list(String)": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#delete(String,String)":{
        type = ErrorType.DELETE;
        break;
      }
      case "FeignApi#update(String,String,Pipeline)": {
        type = ErrorType.UPDATE;
        break;
      }
      case "FeignApi#invalidateCache(String,String)": {
        type = ErrorType.INVALIDATE_CACHE;
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
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)));
  }

  @Getter
  private enum ErrorType {
    LIST("Cannot retrieve pipelines: %s."),
    GET("Cannot retrieve pipeline: %s."),
    DELETE("Cannot delete pipeline: %s."),
    UPDATE("Cannot update pipeline: %s."),
    INVALIDATE_CACHE("Cannot invalidate pipeline cache: %s."),
    LIST_VARIABLES("Cannot list pipeline variables: %s."),
    SET_VARIABLES("Cannot set pipeline variables: %s."),
    UNKNOWN("Pipeline API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

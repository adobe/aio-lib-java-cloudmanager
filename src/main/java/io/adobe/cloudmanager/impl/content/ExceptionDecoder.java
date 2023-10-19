package io.adobe.cloudmanager.impl.content;

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
      case "FeignApi#list(String,int,int)": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#create(String,NewContentSet)": {
        type = ErrorType.CREATE;
        break;
      }
      case "FeignApi#get(String,String)": {
        type = ErrorType.GET;
        break;
      }
      case "FeignApi#update(String,String,NewContentSet)": {
        type = ErrorType.UPDATE;
        break;
      }
      case "FeignApi#delete(String,String)": {
        type = ErrorType.DELETE;
        break;
      }
      case "FeignApi#listFlows(String)":
      case "FeignApi#listFlows(String,int,int)": {
        type = ErrorType.LIST_FLOWS;
        break;
      }
      case "FeignApi#createFlow(String,String,ContentFlowInput)": {
        type = ErrorType.CREATE_FLOW;
        break;
      }
      case "FeignApi#getFlow(String,String)": {
        type = ErrorType.GET_FLOW;
        break;
      }
      case "FeignApi#cancelFlow(String,String)": {
        type = ErrorType.CANCEL_FLOW;
        break;
      }
    }
    return new CloudManagerApiException(String.format(type.message, getError(response)));
  }

  @Getter
  private enum ErrorType {
    LIST("Cannot list content sets: %s."),
    CREATE("Cannot create content set: %s."),
    GET("Cannot get content set: %s."),
    UPDATE("Cannot update content set: %s."),
    DELETE("Cannot delete content set: %s."),
    LIST_FLOWS("Cannot list content flows: %s."),
    CREATE_FLOW("Cannot start content flow: %s."),
    GET_FLOW("Cannot get content flow: %s."),
    CANCEL_FLOW("Cannot cancel content flow: %s."),
    UNKNOWN("Content Set API Error: %s.");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}

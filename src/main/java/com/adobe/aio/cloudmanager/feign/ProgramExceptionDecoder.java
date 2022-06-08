package com.adobe.aio.cloudmanager.feign;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgramExceptionDecoder extends CloudManagerExceptionDecoder {

  private static final Logger logger = LoggerFactory.getLogger(ProgramExceptionDecoder.class);
  
  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status(); 
    ErrorType type = ErrorType.LIST_PROGRAMS;
    switch (methodKey) {
      case "ProgramApiImpl#listPrograms()": {
        type = ErrorType.LIST_PROGRAMS;
        break;
      }
      case "ProgramApiImpl#deleteProgram(String)": {
        type = ErrorType.DELETE_PROGRAM;
        break;
      }
    }

   
    return new CloudManagerApiException(String.format(type.message, getError(response)), status);
  }

  private enum ErrorType {
    LIST_PROGRAMS("Cannot retrieve programs: %s"),
    GET_PROGRAM("Cannot retrieve program: %s"),
    FIND_PROGRAM("Could not find program: %s"),
    DELETE_PROGRAM("Cannot delete program: %s");

    private final String message;

    ErrorType(String message) {
      this.message = message;
    }

  }
}

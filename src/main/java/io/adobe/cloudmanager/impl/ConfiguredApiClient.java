package io.adobe.cloudmanager.impl;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2021 Adobe Inc.
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

import io.adobe.cloudmanager.swagger.invoker.ApiClient;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Performs additional processing of responses that aren't captured/supported by the Swagger generated code.
 */
public class ConfiguredApiClient extends ApiClient {

  public static final String HEADER_REASON = "x-cmapi-reason";

  @Override
  protected Map<String, List<String>> buildResponseHeaders(Response response) {
    Map<String, List<String>> responseHeaders = super.buildResponseHeaders(response);

    // add additional custom headers for error reporting
    responseHeaders.put(HEADER_REASON, Collections.singletonList(response.getStatusInfo().getReasonPhrase()));

    // coerce all header names to lower case
    return responseHeaders.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
  }
}

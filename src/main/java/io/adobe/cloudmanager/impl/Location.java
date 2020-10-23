package io.adobe.cloudmanager.impl;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 Adobe Inc.
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

import lombok.Value;

/*
 * A location URL that may be returned via response headers from the AdobeIO API calls.
 */
@Value
public class Location {

  String url;

  /**
   * Retwrite this URL using the specified base.
   *
   * @param baseUrl the new base URL
   * @return this URL with the replaced base
   */
  public String getRewrittenUrl(String baseUrl) {
    return url.replaceFirst("http(s)?://.*\\.adobe\\.io/", baseUrl + "/");
  }
}

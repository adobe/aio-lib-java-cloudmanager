package io.adobe.cloudmanager;

import java.io.Serializable;

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

import java.security.Key;

import lombok.Value;

@Value
public class AdobeClientCredentials implements Serializable {
    
  private static final long serialVersionUID = -8263353852586958089L;
  String orgId;
  String technicalAccountId;
  String apiKey;
  String clientSecret;
  Key privateKey;
}

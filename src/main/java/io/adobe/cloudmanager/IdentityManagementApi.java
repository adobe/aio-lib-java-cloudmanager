package io.adobe.cloudmanager;

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

public interface IdentityManagementApi {

  String META_SCOPE = "https://ims-na1.adobelogin.com/s/ent_cloudmgr_sdk";
  String ALGORITHM = "RS256";
  String TYPE = "jwt";
  int EXPIRATION = 5; // 5 Minutes

  /**
   * Authenticates to Adobe IMS and returns an Auth Token.
   *
   * @param org the Adobe IMS org context to authenticate against
   * @return the authenticated bearer token
   * @throws IdentityManagementApiException when any error occurs
   */
  String authenticate(AdobeClientCredentials org) throws IdentityManagementApiException;
}

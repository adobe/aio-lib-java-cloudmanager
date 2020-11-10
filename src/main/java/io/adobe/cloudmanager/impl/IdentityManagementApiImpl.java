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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.adobe.cloudmanager.AdobeClientCredentials;
import io.adobe.cloudmanager.IdentityManagementApi;
import io.adobe.cloudmanager.IdentityManagementApiException;
import io.adobe.cloudmanager.jwt.swagger.api.JwtApi;
import io.adobe.cloudmanager.jwt.swagger.invoker.ApiClient;
import io.adobe.cloudmanager.jwt.swagger.invoker.ApiException;
import io.adobe.cloudmanager.jwt.swagger.model.Token;
import io.jsonwebtoken.Jwts;

public class IdentityManagementApiImpl implements IdentityManagementApi {

  private final ApiClient apiClient = new ApiClient();


  public IdentityManagementApiImpl(String baseUrl) {
    apiClient.setBasePath(baseUrl);
  }

  @Override
  public String authenticate(AdobeClientCredentials org) throws IdentityManagementApiException {

    Calendar expires = Calendar.getInstance();
    expires.add(Calendar.MINUTE, EXPIRATION);

    String jws = Jwts.builder()
        .setHeaderParam("alg", ALGORITHM)
        .setHeaderParam("typ", TYPE)
        .setExpiration(expires.getTime())
        .setIssuer(org.getOrgId())
        .setSubject(org.getTechnicalAccountId())
        .setAudience(String.format("%s/c/%s", apiClient.getBasePath(), org.getApiKey()))
        .addClaims(buildClaims())
        .signWith(org.getPrivateKey())
        .compact();
    Token token = authenticate(org, jws);
    return token.getAccessToken();
  }

  private Token authenticate(AdobeClientCredentials org, String jwts) throws IdentityManagementApiException {
    try {
      return new JwtApi(apiClient).authenticate(org.getApiKey(), org.getClientSecret(), jwts);
    } catch (ApiException e) {
      throw new IdentityManagementApiException("Unable to authenticate to AdobeIO.", e) ;
    }
  }

  private Map<String, Object> buildClaims() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(META_SCOPE, true);
    return claims;
  }


}

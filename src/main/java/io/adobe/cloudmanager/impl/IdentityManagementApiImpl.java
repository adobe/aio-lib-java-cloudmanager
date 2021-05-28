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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import io.adobe.cloudmanager.AdobeClientCredentials;
import io.adobe.cloudmanager.IdentityManagementApi;
import io.adobe.cloudmanager.IdentityManagementApiException;
import io.adobe.cloudmanager.ims.generated.api.AdobeImsApi;
import io.adobe.cloudmanager.ims.generated.invoker.ApiClient;
import io.adobe.cloudmanager.ims.generated.invoker.ApiException;
import io.adobe.cloudmanager.ims.generated.invoker.Pair;
import io.adobe.cloudmanager.ims.generated.model.Token;
import io.adobe.cloudmanager.ims.generated.model.User;
import io.jsonwebtoken.Jwts;

public class IdentityManagementApiImpl implements IdentityManagementApi {

  private final ApiClient apiClient = new ApiClient();

  public IdentityManagementApiImpl() {
  }

  public IdentityManagementApiImpl(String baseUrl) {
    apiClient.setBasePath(baseUrl);
  }

  @Override
  public String authenticate(AdobeClientCredentials org) throws IdentityManagementApiException {

    Calendar expires = Calendar.getInstance();
    expires.add(Calendar.MINUTE, EXPIRATION);

    String audienceFormat = StringUtils.endsWith(apiClient.getBasePath(), "/") ? "%sc/%s" :"%s/c/%s";

    String jws = Jwts.builder()
        .setHeaderParam("alg", ALGORITHM)
        .setHeaderParam("typ", TYPE)
        .setExpiration(expires.getTime())
        .setIssuer(org.getOrgId())
        .setSubject(org.getTechnicalAccountId())
        .setAudience(String.format(audienceFormat, apiClient.getBasePath(), org.getApiKey()))
        .addClaims(buildClaims())
        .signWith(org.getPrivateKey())
        .compact();
    Token token = authenticate(org, jws);
    return token.getAccessToken();
  }

  @Override
  public boolean isValid(AdobeClientCredentials org, String accessToken) throws IdentityManagementApiException {
    try {
      List<Pair> queryParams = new ArrayList<>();
      queryParams.add(new Pair("client_id", org.getApiKey()));
      Map<String, String> headers = new HashMap<>();
      headers.put("Authorization", String.format("Bearer %s", accessToken));
      apiClient.invokeAPI("/ims/userinfo/v2", "GET", queryParams, null, headers,  Collections.emptyMap(),"application/json", "application/json", new String[0], new GenericType<User>() {});
      return true;
    } catch (ApiException e) {
      if (Response.Status.UNAUTHORIZED.getStatusCode() != e.getCode()) {
        throw new IdentityManagementApiException("Unable to validate Token", e);
      }
    }
    return false;
  }

  private Token authenticate(AdobeClientCredentials org, String imss) throws IdentityManagementApiException {
    try {
      return new AdobeImsApi(apiClient).authenticate(org.getApiKey(), org.getClientSecret(), imss);
    } catch (ApiException e) {
      throw new IdentityManagementApiException("Unable to authenticate to AdobeIO.", e);
    }
  }

  private Map<String, Object> buildClaims() {
    Map<String, Object> claims = new HashMap<>();
    claims.put(META_SCOPE, true);
    return claims;
  }
}

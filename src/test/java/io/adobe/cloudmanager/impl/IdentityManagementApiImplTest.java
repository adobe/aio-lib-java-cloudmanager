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

import java.net.URLDecoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.AdobeClientCredentials;
import io.adobe.cloudmanager.IdentityManagementApi;
import io.adobe.cloudmanager.IdentityManagementApiException;
import io.adobe.cloudmanager.jwt.swagger.model.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.*;

@ExtendWith(MockServerExtension.class)
public class IdentityManagementApiImplTest {

  private MockServerClient client;
  private String baseUrl;
  private IdentityManagementApi underTest;

  private PrivateKey privateKey;
  private PublicKey publicKey;

  @BeforeEach
  public void beforeEach(MockServerClient client) throws Exception {
    this.client = client;
    this.baseUrl = String.format("http://localhost:%s", client.getPort());
    underTest = new IdentityManagementApiImpl(baseUrl);

    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    KeyPair kp = kpg.generateKeyPair();
    privateKey = kp.getPrivate();
    publicKey = kp.getPublic();
    client.when(
        request()
            .withMethod("POST")
            .withPath("/ims/exchange/jwt")
    ).respond(this::processJwtRequest);
  }

  private HttpResponse processJwtRequest(HttpRequest request) throws Exception {
    String body = request.getBodyAsString();
    String[] pairs = body.split("&");
    Map<String, String> params = new HashMap<>();
    for (String s : pairs) {
      String[] fields = s.split("=");
      String name = URLDecoder.decode(fields[0], "UTF-8");
      String value = URLDecoder.decode(fields[1], "UTF-8");
      params.put(name, value);
    }

    assertNotNull(params.get("client_id"), "Has client_id body parameter.");
    assertNotNull(params.get("client_secret"), "Has client_secret body parameter.");
    String encryptedJwt = params.get("jwt_token");
    assertNotNull(encryptedJwt, "Has jwt_token body parameter.");

    Jwt jwt = Jwts.parserBuilder().setSigningKey(publicKey).build().parse(encryptedJwt);
    assertEquals(IdentityManagementApi.ALGORITHM, jwt.getHeader().get("alg"), "Has correct algorithm");
    assertEquals(IdentityManagementApi.TYPE, jwt.getHeader().get("typ"), "Has correct type.");

    Claims claims = (Claims) jwt.getBody();
    assertNotNull(claims.getIssuer(), "Has OrgID set");
    assertNotNull(claims.getSubject(), "Has Technical Account ID set.");
    assertNotNull(claims.getAudience(), "Has API Key Set.");
    assertTrue((Boolean) claims.get(IdentityManagementApi.META_SCOPE), "Has Claim scope set.");

    Token bearer = new Token();
    bearer.setAccessToken("Access Token");
    bearer.setTokenType("bearer");
    bearer.setExpiresIn(claims.getExpiration().getTime());
    return HttpResponse.response()
        .withStatusCode(HttpStatusCode.OK_200.code())
        .withContentType(MediaType.APPLICATION_JSON)
        .withBody(new ObjectMapper().writeValueAsString(bearer));

  }

  @Test
  void testAuthenticate() throws IdentityManagementApiException {
    AdobeClientCredentials org = new AdobeClientCredentials("AdobeImsOrg@AdobeOrg", "1234567890@techacct.adobe.com", "9876543210987654321", "12345678-9abc-def0-1234-56789abcdef0", privateKey);
    String token = underTest.authenticate(org);
    assertEquals("Access Token", token, "Access token matched.");
  }

}

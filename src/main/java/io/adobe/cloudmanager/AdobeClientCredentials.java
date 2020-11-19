package io.adobe.cloudmanager;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

import lombok.Value;

@Value
public class AdobeClientCredentials {

  String orgId;
  String technicalAccountId;
  String apiKey;
  String clientSecret;
  PrivateKey privateKey;

  /**
   * Generates a private key object from a PEM encoded PKCS#8 key string.
   *
   * @param pem the private key as string in format specified by RFC 7468, section 10
   * @return the private key
   * @throws IOException              if the string can't be read
   * @throws NoSuchAlgorithmException if the crypto library doesn't support the algorithm
   * @throws InvalidKeySpecException  if the reader doesn't contain a key of the specified type
   * @see <a href="https://tools.ietf.org/html/rfc7468#section-10">RFC 7468</a>
   */
  public static PrivateKey getKeyFromPem(String pem) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    try (BufferedReader pemReader = new BufferedReader(new StringReader(pem))) {
      return getKeyFromPem(pemReader);
    }
  }

  /**
   * Generates a private key object from a PEM encoded PKCS#8 key string.
   *
   * @param pemReader the private key as buffered reader in format specified by RFC 7468, section 10
   * @return the private key
   * @throws NoSuchAlgorithmException if the crypto library doesn't support the algorithm
   * @throws InvalidKeySpecException  if the reader doesn't contain a key of the specified type
   * @see <a href="https://tools.ietf.org/html/rfc7468#section-10">RFC 7468</a>
   */
  public static PrivateKey getKeyFromPem(BufferedReader pemReader) throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] encodedKey = convertPemToDer(pemReader);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    KeySpec ks = new PKCS8EncodedKeySpec(encodedKey);
    return keyFactory.generatePrivate(ks);
  }

  private static byte[] convertPemToDer(BufferedReader reader) {
    String base64 = reader.lines()
        .filter(line -> !line.startsWith("-----BEGIN") && !line.startsWith("-----END"))
        .collect(Collectors.joining());

    Base64.Decoder decoder = Base64.getDecoder();
    return decoder.decode(base64);
  }
}

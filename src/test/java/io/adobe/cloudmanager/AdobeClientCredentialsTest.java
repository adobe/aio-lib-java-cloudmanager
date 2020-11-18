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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AdobeClientCredentialsTest {

    @Test
    public void testGetKeyFromPem() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String pem = IOUtils.resourceToString("/keys/private-key-pkcs8-rsa.pem", StandardCharsets.US_ASCII);
        PrivateKey actualKey = AdobeClientCredentials.getKeyFromPem(pem);
        
        byte[] derBytes = IOUtils.resourceToByteArray("/keys/private-key-pkcs8-rsa.der");
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        KeySpec ks = new PKCS8EncodedKeySpec(derBytes);
        PrivateKey expectedKey =  keyFactory.generatePrivate(ks);
        Assertions.assertEquals(expectedKey, actualKey);
    }
}

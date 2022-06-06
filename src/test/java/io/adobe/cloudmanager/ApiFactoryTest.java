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

import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.HashSet;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.impl.CloudManagerApiImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiFactoryTest {

  private static final String DUMMY = "DUMMY";
  private static PrivateKey privateKey;
  static {
    try {
      KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
      kpGen.initialize(2048);
      KeyPair keyPair = kpGen.generateKeyPair();
      privateKey = keyPair.getPrivate();
      
    } catch (NoSuchAlgorithmException e) {
    }
  }
  @Test
  public void testCloudManagerFactory() {

    Workspace workspace = Workspace.builder()
        .apiKey(DUMMY)
        .clientSecret(DUMMY)
        .imsOrgId(DUMMY)
        .technicalAccountId(DUMMY)
        .privateKey(privateKey)
        .build();
    assertNotNull(CloudManagerApi.create(workspace));
  }

  @Test
  public void testCloudManagerFactoryBaseUrl() throws Exception {
    Workspace workspace = Workspace.builder()
        .apiKey(DUMMY)
        .clientSecret(DUMMY)
        .imsOrgId(DUMMY)
        .technicalAccountId(DUMMY)
        .privateKey(privateKey)
        .build();
    assertNotNull(CloudManagerApi.create(workspace, new URL(CloudManagerApi.BASE_URL)));
  }
}

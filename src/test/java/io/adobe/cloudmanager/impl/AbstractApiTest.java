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


import io.adobe.cloudmanager.CloudManagerApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

@ExtendWith(MockServerExtension.class)
public abstract class AbstractApiTest {

  protected MockServerClient client;
  protected String baseUrl;
  protected CloudManagerApi underTest;

  @BeforeEach
  public void beforeEach(MockServerClient client) {
    this.client = client;
    this.baseUrl = String.format("http://localhost:%s", client.getPort());
    underTest = CloudManagerApi.create("success", "test-apikey", "test-token", baseUrl + "/");
  }
}

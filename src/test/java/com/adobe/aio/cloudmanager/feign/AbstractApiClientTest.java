package com.adobe.aio.cloudmanager.feign;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
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

import java.net.MalformedURLException;
import java.net.URL;

import com.adobe.aio.cloudmanager.CloudManagerApi;
import com.adobe.aio.ims.feign.JWTAuthInterceptor;
import com.adobe.aio.workspace.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import static org.mockito.Mockito.*;

@ExtendWith({MockServerExtension.class, MockitoExtension.class})
public class AbstractApiClientTest {
  protected MockServerClient client;
  protected String baseUrl;
  protected CloudManagerApi underTest;

  @Mock(strictness = Mock.Strictness.LENIENT)
  protected Workspace workspace;
  @Mock
  private JWTAuthInterceptor.Builder jwtBuilder;
  @Mock
  private JWTAuthInterceptor interceptor;

  @BeforeEach
  public void beforeEach(MockServerClient client) throws MalformedURLException {

    try (MockedStatic<JWTAuthInterceptor> mocked = mockStatic(JWTAuthInterceptor.class)) {
      mocked.when(JWTAuthInterceptor::builder).thenReturn(jwtBuilder);
      when(jwtBuilder.workspace(workspace)).thenReturn(jwtBuilder);
      when(jwtBuilder.build()).thenReturn(interceptor);

      this.client = client;
      this.baseUrl = String.format("http://localhost:%s", client.getPort());
      when(workspace.getImsOrgId()).thenReturn("success");
      when(workspace.getApiKey()).thenReturn("test-apikey");

      underTest = CloudManagerApi.create(workspace, new URL(baseUrl));
    }
    verify(workspace).validateJwtCredentialConfig();
  }

}

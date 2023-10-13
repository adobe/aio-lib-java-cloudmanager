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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.adobe.aio.auth.Context;
import com.adobe.aio.ims.feign.AuthInterceptor;
import com.adobe.aio.ims.feign.OAuthInterceptor;
import com.adobe.aio.workspace.Workspace;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.JsonBody;

import static com.adobe.aio.util.Constants.*;
import static org.mockito.Mockito.*;

@ExtendWith({ MockitoExtension.class, MockServerExtension.class })
public abstract class AbstractApiTest {

  protected MockServerClient client;
  protected String baseUrl;

  @Mock
  protected Workspace workspace;

  @Mock
  private Context authContext;

  protected AuthInterceptor authInterceptor = new OAuthInterceptor(null) {
    @Override
    public void apply(RequestTemplate requestTemplate) {
      if (requestTemplate.headers().containsKey(AUTHORIZATION_HEADER)) {
        return;
      }
      requestTemplate.header(AUTHORIZATION_HEADER, "Bearer test-token");
    }
  };

  @BeforeEach
  void before(MockServerClient client) {
    this.client = client;
    this.baseUrl = String.format("http://localhost:%s", client.getPort());
    when(workspace.getAuthContext()).thenReturn(authContext);
    doNothing().when(authContext).validate();
  }


  protected static JsonBody loadBodyJson(String filePath) {
    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream(filePath)) {
      assert is != null;
      return JsonBody.json(IOUtils.toString(is, Charset.defaultCharset()));
    } catch (IOException e) {
      throw new TestInstantiationException(e.getMessage());
    }
  }
}

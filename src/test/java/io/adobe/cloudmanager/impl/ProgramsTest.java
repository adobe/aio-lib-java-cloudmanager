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

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import com.adobe.aio.ims.ImsService;
import com.adobe.aio.ims.model.AccessToken;
import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.junit.jupiter.MockServerExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;

@ExtendWith({ MockServerExtension.class, MockitoExtension.class })
class ProgramsTest extends AbstractApiTest {

  @Mock
  protected Workspace workspace;

  public static Collection<String> getTestExpectationFiles() {
    return Arrays.asList(
        "programs/not-found.json",
        "programs/forbidden.json",
        "programs/forbidden-code-only.json",
        "programs/forbidden-message-only.json",
        "programs/empty-response.json",
        "programs/delete-fails.json",
        "programs/delete-success.json"
    );
  }

  @Test
  void listPrograms_failure404() {
    CloudManagerApi api = new CloudManagerApiImpl("not-found", "test-apikey", "test-token", baseUrl);

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403() {
    CloudManagerApi api = CloudManagerApi.create("forbidden", "test-apikey", "test-token", baseUrl);

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden) - Detail: some message (Code: 1234)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403_errorMessageOnly() {
    CloudManagerApi api = CloudManagerApi.create("forbidden-messageonly", "test-apikey", "test-token", baseUrl);

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden) - Detail: some message", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403_errorCodeOnly() {
    CloudManagerApi api = CloudManagerApi.create("forbidden-codeonly", "test-apikey", "test-token", baseUrl);

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_successEmpty() throws CloudManagerApiException {
    CloudManagerApi api = CloudManagerApi.create("empty", "test-apikey", "test-token", baseUrl);

    Collection<Program> programs = api.listPrograms();
    assertTrue(programs.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void listPrograms_success() throws CloudManagerApiException {
    Collection<Program> programs = underTest.listPrograms();
    assertEquals(7, programs.size(), "Correct length of program list");
  }

  @Test
  void deleteProgram_failure() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.deleteProgram("2"), "Exception was thrown");
    assertEquals(String.format("Cannot delete program: %s/api/program/2 (400 Bad Request)", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(request().withMethod("DELETE").withPath("/api/program/2"));
  }

  @Test
  void deleteProgram_notFound() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.deleteProgram("11"), "Exception was thrown");
    assertEquals("Could not find program 11", exception.getMessage(), "Correct exception message");
  }

  @Test
  void deleteProgram_success() throws CloudManagerApiException {
    underTest.deleteProgram("3");
    client.verify(request().withMethod("DELETE").withPath("/api/program/3"));
  }

  @Test
  void deleteProgram_viaProgram() throws Exception {
    Collection<Program> programs = underTest.listPrograms();
    Program program = programs.stream().filter(p -> p.getId().equals("3")).findFirst().orElseThrow(Exception::new);
    program.delete();
    client.verify(request().withMethod("DELETE").withPath("/api/program/3"));
  }

  @Test
  void listPrograms_oauth_noToken(
      @Mock ImsService imsService
  ) throws Exception {
    when(workspace.getImsOrgId()).thenReturn("success");
    when(workspace.getApiKey()).thenReturn("test-apikey");

    AccessToken token = new AccessToken("test-token", 300000L);

    try (MockedConstruction<ImsService.Builder> ignored = mockConstruction(ImsService.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(imsService);
        }
    )) {
      when(imsService.getOAuthAccessToken()).thenReturn(token);
      Collection<Program> programs = new CloudManagerApiImpl(workspace, new URL(baseUrl)).listPrograms();
      assertEquals(7, programs.size(), "Correct length of program list");
    }
  }

  @Test
  void listPrograms_oauth_expired(
      @Mock ImsService imsService
  ) throws Exception {
    when(workspace.getImsOrgId()).thenReturn("success");
    when(workspace.getApiKey()).thenReturn("test-apikey");

    try (MockedConstruction<ImsService.Builder> ignored = mockConstruction(ImsService.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(imsService);
        }
    )) {
      CloudManagerApi api =  new CloudManagerApiImpl(workspace, new URL(baseUrl));
      AccessToken token = new AccessToken("test-token", 300000L);
      Field tokenField = CloudManagerApiImpl.class.getDeclaredField("token");
      tokenField.setAccessible(true);
      tokenField.set(api, token);

      when(imsService.getOAuthAccessToken()).thenReturn(token);
      Collection<Program> programs = api.listPrograms();
      assertEquals(7, programs.size(), "Correct length of program list");
    }
  }

  @Test
  void listPrograms_oauth(
      @Mock ImsService imsService
  ) throws Exception {
    when(workspace.getImsOrgId()).thenReturn("success");
    when(workspace.getApiKey()).thenReturn("test-apikey");

    try (MockedConstruction<ImsService.Builder> ignored = mockConstruction(ImsService.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(imsService);
        }
    )) {
      CloudManagerApi api =  new CloudManagerApiImpl(workspace, new URL(baseUrl));
      AccessToken token = new AccessToken("test-token", 300000L);
      Field tokenField = CloudManagerApiImpl.class.getDeclaredField("token");
      tokenField.setAccessible(true);
      tokenField.set(api, token);

      Field expiresField = CloudManagerApiImpl.class.getDeclaredField("expiration");
      expiresField.setAccessible(true);
      expiresField.set(api, System.currentTimeMillis() + token.getExpiresIn());

      Collection<Program> programs = api.listPrograms();
      assertEquals(7, programs.size(), "Correct length of program list");
    }
  }
}

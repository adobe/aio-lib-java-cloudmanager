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

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.model.EmbeddedProgram;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;

@ExtendWith(MockServerExtension.class)
class ProgramsTest extends AbstractApiTest {

  @Test
  void listPrograms_failure404() {
    CloudManagerApi api = new CloudManagerApiImpl("not-found", "test-apikey", "test-token", baseUrl);

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403() {
    CloudManagerApi api = new CloudManagerApiImpl("forbidden", "test-apikey", "test-token", baseUrl);

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden) - Detail: some message (Code: 1234)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403_errorMessageOnly() {
    CloudManagerApi api = new CloudManagerApiImpl("forbidden-messageonly", "test-apikey", "test-token", baseUrl);

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden) - Detail: some message", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403_errorCodeOnly() {
    CloudManagerApi api = new CloudManagerApiImpl("forbidden-codeonly", "test-apikey", "test-token", baseUrl);

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_successEmpty() throws CloudManagerApiException {
    CloudManagerApi api = new CloudManagerApiImpl("empty", "test-apikey", "test-token", baseUrl);

    List<EmbeddedProgram> programs = api.listPrograms();
    assertTrue(programs.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void listPrograms_success() throws CloudManagerApiException {
    List<EmbeddedProgram> programs = underTest.listPrograms();
    assertEquals(4, programs.size(), "Correct length of program list");
  }

  @Test
  void deleteProgram_failure() throws CloudManagerApiException {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.deleteProgram("5"), "Exception was thrown");
    assertEquals(String.format("Cannot delete program: %s/api/program/5 (400 Bad Request)", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(request().withMethod("DELETE").withPath("/api/program/5"));
  }

  @Test
  void deleteProgram_notFound() throws CloudManagerApiException {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class,
        () -> underTest.deleteProgram("11"), "Exception was thrown");
    assertEquals("Could not find program 11", exception.getMessage(), "Correct exception message");
  }

  @Test
  void deleteProgram_success() throws CloudManagerApiException {
    underTest.deleteProgram("6");
    client.verify(request().withMethod("DELETE").withPath("/api/program/6"));
  }

  @Test
  void deleteProgram_viaProgram() throws Exception {
    List<EmbeddedProgram> programs = underTest.listPrograms();
    EmbeddedProgram program = programs.stream().filter(p -> p.getId().equals("6")).findFirst().orElseThrow(Exception::new);
    program.delete();
    client.verify(request().withMethod("DELETE").withPath("/api/program/6"));
  }
}

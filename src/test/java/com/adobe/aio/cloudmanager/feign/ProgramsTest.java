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

import java.util.Arrays;
import java.util.Collection;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Program;
import org.junit.jupiter.api.Test;
import org.mockserver.model.ClearType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;

public class ProgramsTest extends AbstractApiClientTest {

  public static Collection<String> getTestExpectationFiles() {
    return Arrays.asList(
        "programs/not-found.json",
        "programs/forbidden.json",
        "programs/forbidden-code-only.json",
        "programs/forbidden-message-only.json",
        "programs/empty-response.json",
        "programs/list-success.json",
        "programs/get-success.json",
        "programs/delete-fails.json",
        "programs/delete-success.json"
    );
  }

  @Test
  void listPrograms_failure404() {
    when(workspace.getImsOrgId()).thenReturn("list-not-found");

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listPrograms, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403() {
    when(workspace.getImsOrgId()).thenReturn("forbidden");

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listPrograms, "Exception thrown for 403");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden) - Detail: some message (Code: 1234)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403_errorMessageOnly() {
    when(workspace.getImsOrgId()).thenReturn("forbidden-messageonly");

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listPrograms, "Exception thrown for 403");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden) - Detail: some message", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_failure403_errorCodeOnly() {
    when(workspace.getImsOrgId()).thenReturn("forbidden-codeonly");

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listPrograms, "Exception thrown for 403");
    assertEquals(String.format("Cannot retrieve programs: %s/api/programs (403 Forbidden)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_successEmpty() throws CloudManagerApiException {
    when(workspace.getImsOrgId()).thenReturn("empty");

    Collection<Program> programs = underTest.listPrograms();
    assertTrue(programs.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void listPrograms_success() throws CloudManagerApiException {
    Collection<Program> programs = underTest.listPrograms();
    assertEquals(7, programs.size(), "Correct length of program list");
  }

  @Test
  void getProgram_failure404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getProgram("99"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve program: %s/api/program/99 (404 Not Found)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void getProgram_success() throws CloudManagerApiException {
    Program program = underTest.getProgram("1");
    assertNotNull(program, "Program retrieval success.");
  }
  
  @Test
  void deleteProgram_failure400() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteProgram("2"), "Exception was thrown");
    assertEquals(String.format("Cannot delete program: %s/api/program/2 (400 Bad Request)", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(request().withMethod("DELETE").withPath("/api/program/2"));
    client.clear(request().withPath("/api/program/2"), ClearType.LOG);
  }

  @Test
  void deleteProgram_success() throws CloudManagerApiException {
    underTest.deleteProgram("3");
    client.verify(request().withMethod("DELETE").withPath("/api/program/3"));
    client.clear(request().withPath("/api/program/3"), ClearType.LOG);
  }

  @Test
  void deleteProgram_viaProgram() throws Exception {
    Collection<Program> programs = underTest.listPrograms();
    Program program = programs.stream().filter(p -> p.getId().equals("3")).findFirst().orElseThrow(Exception::new);
    program.delete();
    client.verify(request().withMethod("DELETE").withPath("/api/program/3"));
  }
}

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

import java.util.Arrays;
import java.util.Collection;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.Tenant;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;

class ProgramsTest extends AbstractApiTest {

  public static Collection<String> getTestExpectationFiles() {
    return Arrays.asList(
        "programs/delete-fails.json",
        "programs/delete-success.json",
        "programs/not-found.json",
        "programs/empty-response.json"
    );
  }

  @Test
  void deleteProgram_failure() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteProgram("2"), "Exception was thrown");
    assertEquals(String.format("Cannot delete program: %s/api/program/2 (400 Bad Request).", baseUrl), exception.getMessage(), "Correct exception message");
    client.verify(request().withMethod("DELETE").withPath("/api/program/2"));
  }

  @Test
  void deleteProgram_success() throws CloudManagerApiException {
    underTest.deleteProgram("3");
    client.verify(request().withMethod("DELETE").withPath("/api/program/3"));
  }

  @Test
  void deleteProgram_viaProgram(@Mock io.adobe.cloudmanager.impl.generated.EmbeddedProgram mock) throws Exception {
    when(mock.getId()).thenReturn("4");
    Program program = new ProgramImpl(mock, underTest);
    program.delete();
    client.verify(request().withMethod("DELETE").withPath("/api/program/4"));
  }

  @Test
  void listPrograms_failure404() {
    when(workspace.getImsOrgId()).thenReturn("not-found");
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listPrograms("1"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve programs: %s/api/tenant/1/programs (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listPrograms_successEmpty() throws CloudManagerApiException {
    when(workspace.getImsOrgId()).thenReturn("empty");
    Collection<Program> programs = underTest.listPrograms("1");
    assertTrue(programs.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void listPrograms_success() throws CloudManagerApiException {
    Collection<Program> programs = underTest.listPrograms("1");
    assertEquals(7, programs.size(), "Correct length of program list");
  }

  @Test
  void listPrograms_fromTenant(@Mock io.adobe.cloudmanager.impl.generated.Tenant mock) throws CloudManagerApiException {
    when(mock.getId()).thenReturn("1");
    Tenant tenant = new TenantImpl(mock, underTest);
    Collection<Program> programs = tenant.listPrograms();
    assertEquals(7, programs.size(), "Correct length of program list");
  }
}

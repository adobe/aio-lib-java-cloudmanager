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

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.junit.jupiter.MockServerExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.*;

@ExtendWith(MockServerExtension.class)
class ProgramsTest extends AbstractApiTest {

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
  void deleteProgram_viaProgram() throws Exception {
    Collection<Program> programs = underTest.listPrograms();
    Program program = programs.stream().filter(p -> p.getId().equals("3")).findFirst().orElseThrow(Exception::new);
    program.delete();
    client.verify(request().withMethod("DELETE").withPath("/api/program/3"));
  }
}

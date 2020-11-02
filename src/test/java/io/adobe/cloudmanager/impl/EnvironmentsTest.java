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

import java.util.Arrays;
import java.util.List;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.model.Environment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.*;

class EnvironmentsTest extends AbstractApiTest {

  public static List<String> getTestExpectationFiles() {
    return Arrays.asList(
        "environments/not-found.json",
        "environments/list-empty.json",
        "environments/list-success.json",
        "environments/delete-fails.json",
        "environments/delete-success.json"
    );
  }

  @Test
  void listEnvironments_failure() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("1"), "Exception thrown for 404");
    assertEquals(String.format("Could not find environments: %s/api/program/1/environments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listEnvironments_successEmpty() throws CloudManagerApiException {
    List<Environment> environments = underTest.listEnvironments("3");
    assertTrue(environments.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void listEnvironments_success() throws CloudManagerApiException {
    List<Environment> environments = underTest.listEnvironments("2");
    assertEquals(5, environments.size(), "Correct environment length list");
  }

  @Test
  void listEnvironments_badProgram() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("8"), "Exception thrown for 404");
    assertEquals("Could not find program 8", exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_deleteReturns400() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteEnvironment("2", "3"), "Exception thrown for 404");
    assertEquals(String.format("Cannot delete environment: %s/api/program/2/environment/3 (400 Bad Request)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_badEnvironment() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteEnvironment("2", "12"), "Exception thrown for 404");
    assertEquals("Could not find environment 12 for program 2.", exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_success() throws CloudManagerApiException {
    underTest.deleteEnvironment("2", "1");

    client.verify(request().withMethod("DELETE").withPath("/api/program/2/environment/1"));
  }

  @Test
  void getDeveloperConsoleUrl_missing() throws Exception {
    List<Environment> environments = underTest.listEnvironments("2");
    Environment environment = environments.stream().filter(e -> e.getId().equals("3")).findFirst().orElseThrow(Exception::new);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> environment.getDeveloperConsoleUrl(), "Exception thrown");
    assertEquals("Environment 3 does not appear to support Developer Console.", exception.getMessage(), "Exception message is correct");
  }

  @Test
  void getDeveloperConsoleUrl_success() throws Exception {
    List<Environment> environments = underTest.listEnvironments("2");
    Environment environment = environments.stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    String url = environment.getDeveloperConsoleUrl();
    assertEquals("https://github.com/adobe/aio-cli-plugin-cloudmanager", url, "URL correctly read");
  }

}

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

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.model.Environment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;

class EnvironmentsTest extends AbstractApiTest {

  @Test
  void listEnvironments_failure() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("6"), "Exception thrown for 404");
    assertEquals(String.format("Could not find environments: %s/api/program/6/environments (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void listEnvironments_successEmpty() throws CloudManagerApiException {
    List<Environment> environments = underTest.listEnvironments("5");
    assertTrue(environments.isEmpty(), "Empty body returns zero length list");
  }

  @Test
  void listEnvironments_success() throws CloudManagerApiException {
    List<Environment> environments = underTest.listEnvironments("4");
    assertEquals(5, environments.size(), "Correct environment length list");
  }

  @Test
  void listEnvironments_badProgram() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironments("8"), "Exception thrown for 404");
    assertEquals("Could not find program 8", exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_deleteReturns400() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteEnvironment("4", "3"), "Exception thrown for 404");
    assertEquals(String.format("Cannot delete environment: %s/api/program/4/environment/3 (400 Bad Request)", baseUrl), exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_badEnvironment() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.deleteEnvironment("4", "12"), "Exception thrown for 404");
    assertEquals("Could not find environment 12 for program 4.", exception.getMessage(), "Message was correct");
  }

  @Test
  void deleteEnvironment_success() throws CloudManagerApiException {
    underTest.deleteEnvironment("4", "11");

    client.verify(request().withMethod("DELETE").withPath("/api/program/4/environment/11"));
  }

  @Test
  void getDeveloperConsoleUrl_missing() throws Exception {
    List<Environment> environments = underTest.listEnvironments("4");
    Environment environment = environments.stream().filter(e -> e.getId().equals("3")).findFirst().orElseThrow(Exception::new);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> environment.getDeveloperConsoleUrl(), "Exception thrown");
    assertEquals("Environment 3 does not appear to support Developer Console.", exception.getMessage(), "Exception message is correct");
  }

  @Test
  void getDeveloperConsoleUrl_success() throws Exception {
    List<Environment> environments = underTest.listEnvironments("4");
    Environment environment = environments.stream().filter(e -> e.getId().equals("1")).findFirst().orElseThrow(Exception::new);
    String url = environment.getDeveloperConsoleUrl();
    assertEquals("https://github.com/adobe/aio-cli-plugin-cloudmanager", url, "URL correctly read");
  }

}

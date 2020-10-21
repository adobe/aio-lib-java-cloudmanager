package io.adobe.cloudmanager.impl;

import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.model.EmbeddedProgram;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
}
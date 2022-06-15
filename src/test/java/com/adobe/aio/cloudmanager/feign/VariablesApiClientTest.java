package com.adobe.aio.cloudmanager.feign;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VariablesApiClientTest extends AbstractApiClientTest {
  
  @Test
  void getEnvironmentVariables_failure404() {
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listEnvironmentVariables("1", "1"), "Exception thrown.");
    assertEquals(String.format("Could not find environment: %s/api/program/1/environment/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
  }
}

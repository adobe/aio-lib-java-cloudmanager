package io.adobe.cloudmanager.impl;


import io.adobe.cloudmanager.CloudManagerApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

@ExtendWith(MockServerExtension.class)
public abstract class AbstractApiTest {

  protected MockServerClient client;
  protected String baseUrl;
  protected CloudManagerApi underTest;

  @BeforeEach
  public void beforeEach(MockServerClient client) {
    this.client = client;
    this.baseUrl = String.format("http://localhost:%s", client.getPort());
    underTest = new CloudManagerApiImpl("success", "test-apikey", "test-token", baseUrl);
  }
}

package io.adobe.cloudmanager.impl;

import io.adobe.cloudmanager.swagger.invoker.ApiClient;
import io.adobe.cloudmanager.util.LocationMessageBodyReader;
import org.glassfish.jersey.client.ClientConfig;

public class ConfiguredApiClient extends ApiClient {

  @Override
  protected void performAdditionalClientConfiguration(ClientConfig clientConfig) {
    clientConfig.register(LocationMessageBodyReader.class);
  }
}

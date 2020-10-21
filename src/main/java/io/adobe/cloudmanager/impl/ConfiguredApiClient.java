package io.adobe.cloudmanager.impl;

import io.adobe.cloudmanager.swagger.invoker.ApiClient;
import io.adobe.cloudmanager.util.LocationMessageBodyReader;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfiguredApiClient extends ApiClient {

  public static final String HEADER_REASON = "x-cmapi-reason";

  @Override
  protected void performAdditionalClientConfiguration(ClientConfig clientConfig) {
    clientConfig.register(LocationMessageBodyReader.class);
  }

  @Override
  protected Map<String, List<String>> buildResponseHeaders(Response response) {
    Map<String, List<String>> responseHeaders = super.buildResponseHeaders(response);

    // add additional custom headers for error reporting
    responseHeaders.put(HEADER_REASON, Collections.singletonList(response.getStatusInfo().getReasonPhrase()));

    // coerce all header names to lower case
    return responseHeaders.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
  }
}

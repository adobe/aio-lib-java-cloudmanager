package io.adobe.cloudmanager.impl;

import java.util.Collection;
import java.util.UUID;

import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Tenant;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class TenantTest extends AbstractApiTest {
  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listTenants, "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve tenants: %s/api/tenants (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"error_code\":\"1234\", \"message\":\"some message\" }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listTenants, "Exception thrown for 403");
    assertEquals(String.format("Cannot retrieve tenants: %s/api/tenants (403 Forbidden) - Detail: some message (Code: 1234).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_403_errorMessageOnly() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"message\":\"some message\" }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listTenants, "Exception thrown for 403");
    assertEquals(String.format("Cannot retrieve tenants: %s/api/tenants (403 Forbidden) - Detail: some message.", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_403_errorCodeOnly() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"error_code\":\"1234\" }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::listTenants, "Exception thrown for 403");
    assertEquals(String.format("Cannot retrieve tenants: %s/api/tenants (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }


  @Test
  void list_success_empty() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withBody(json("{}")));
    Collection<Tenant> tenants = underTest.listTenants();
    assertEquals(0, tenants.size(), "Correct length of tenant list");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withBody(loadBodyJson("tenant/list.json")));
    Collection<Tenant> tenants = underTest.listTenants();
    assertEquals(1, tenants.size(), "Correct length of tenant list");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getTenant("1"), "Exception thrown for 404");
    assertEquals(String.format("Cannot retrieve tenant: %s/api/tenant/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1");
    client.when(get).respond(response().withBody(loadBodyJson("tenant/get.json")));
    assertNotNull(underTest.getTenant("1"), "Tenant retrieved");
    client.verify(get);
    client.clear(get);
  }


}

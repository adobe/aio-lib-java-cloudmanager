package io.adobe.cloudmanager.impl;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import com.adobe.aio.ims.feign.AuthInterceptor;
import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Tenant;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TenantTest extends AbstractApiTest {
  public static Collection<String> getTestExpectationFiles() {
    return Arrays.asList(
        "tenant/not-found.json",
        "tenant/empty-response.json",
        "tenant/forbidden.json",
        "tenant/forbidden-code-only.json",
        "tenant/forbidden-message-only.json"
    );
  }

  @Test
  void list_failure404() throws Exception {
    when(workspace.getImsOrgId()).thenReturn("not-found");
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      CloudManagerApi api = CloudManagerApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
      CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listTenants, "Exception thrown for 404");
      assertEquals(String.format("Cannot retrieve tenants: %s/api/tenants (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    }
  }


  @Test
  void list_failure403() throws Exception {
    when(workspace.getImsOrgId()).thenReturn("forbidden");
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      CloudManagerApi api = CloudManagerApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
      CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listTenants, "Exception thrown for 403");
      assertEquals(String.format("Cannot retrieve tenants: %s/api/tenants (403 Forbidden) - Detail: some message (Code: 1234).", baseUrl), exception.getMessage(), "Message was correct");
    }
  }

  @Test
  void list_failure403_errorMessageOnly() throws Exception {
    when(workspace.getImsOrgId()).thenReturn("forbidden-messageonly");
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      CloudManagerApi api = CloudManagerApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
      CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listTenants, "Exception thrown for 403");
      assertEquals(String.format("Cannot retrieve tenants: %s/api/tenants (403 Forbidden) - Detail: some message.", baseUrl), exception.getMessage(), "Message was correct");
    }
  }

  @Test
  void list_failure403_errorCodeOnly() throws Exception {
    when(workspace.getImsOrgId()).thenReturn("forbidden-codeonly");
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      CloudManagerApi api = CloudManagerApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
      CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, api::listTenants, "Exception thrown for 403");
      assertEquals(String.format("Cannot retrieve tenants: %s/api/tenants (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
    }
  }


  @Test
  void list_success_empty() throws Exception {
    when(workspace.getImsOrgId()).thenReturn("empty");

    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      CloudManagerApi api = CloudManagerApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
      Collection<Tenant> list = api.listTenants();
      assertEquals(0, list.size(), "Correct length of tenant list");
    }
  }

  @Test
  void list_success() throws Exception {
    Collection<Tenant> list = underTest.listTenants();
    assertEquals(1, list.size(), "Correct length of tenant list");
  }

  @Test
  void get_failure404() throws Exception {
    when(workspace.getImsOrgId()).thenReturn("not-found");
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      CloudManagerApi api = CloudManagerApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
      CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> api.getTenant("1"), "Exception thrown for 404");
      assertEquals(String.format("Cannot retrieve tenant: %s/api/tenant/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    }
  }

  @Test
  void get_success() throws Exception {
    assertNotNull(underTest.getTenant("1"), "Tenant retrieved");
  }


}

package io.adobe.cloudmanager;

import com.adobe.aio.auth.Context;
import com.adobe.aio.workspace.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiBuilderTest {

  @Test
  void no_workspace() {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new ApiBuilder<>(ApiBuilder.class).build(), "Exception thrown.");
    assertEquals("Workspace must be specified.", exception.getMessage(), "Message was correct.");
  }

  @Test
  void no_auth_context(@Mock Workspace workspace) {
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new ApiBuilder<>(ApiBuilder.class).workspace(workspace).build(), "Exception thrown.");
    assertEquals("Workspace must specify AuthContext.", exception.getMessage(), "Message was correct.");
  }

  @Test
  void unknown_type(@Mock Workspace workspace, @Mock Context authContext) {
    when(workspace.getAuthContext()).thenReturn(authContext);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> new ApiBuilder<>(ApiBuilder.class).workspace(workspace).build(), "Exception thrown.");
    assertEquals("Unknown API requested (class io.adobe.cloudmanager.ApiBuilder).", exception.getMessage(), "Message was correct.");

  }
}

package io.adobe.cloudmanager;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
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

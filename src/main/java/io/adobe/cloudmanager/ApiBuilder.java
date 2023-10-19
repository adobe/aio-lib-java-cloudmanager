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

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.validation.constraints.NotNull;

import com.adobe.aio.workspace.Workspace;
import io.adobe.cloudmanager.impl.content.ContentSetApiImpl;
import io.adobe.cloudmanager.impl.environment.EnvironmentApiImpl;
import io.adobe.cloudmanager.impl.pipeline.PipelineApiImpl;
import io.adobe.cloudmanager.impl.pipeline.execution.PipelineExecutionApiImpl;
import io.adobe.cloudmanager.impl.program.ProgramApiImpl;
import io.adobe.cloudmanager.impl.repository.RepositoryApiImpl;
import io.adobe.cloudmanager.impl.tenant.TenantApiImpl;

/**
 * Builder for creating instances of the Cloud Manager APIs
 *
 * @param <A> the type of API to create
 */
public class ApiBuilder<A> {
  private final Class<A> clazz;
  private Workspace workspace;
  private URL url;

  /**
   * Create new instance of an API Builder, scoped to the desired interface.
   *
   * @param clazz the type of API desired.
   */
  public ApiBuilder(Class<A> clazz) {
    this.clazz = clazz;
  }

  /**
   * (Required) Workspace context for the API.
   *
   * @param workspace the workspace
   * @return this builder
   */
  public ApiBuilder<A> workspace(@NotNull Workspace workspace) {
    this.workspace = workspace;
    return this;
  }

  /**
   * (Optional) Base url for this API. Can be used to change destination for requests, to the Stage environment for example.
   * If not provided, then the default production value will be used.wqa9]
   *
   * @param url the url context for requests
   * @return this builder
   */
  public ApiBuilder<A> url(@NotNull URL url) {
    this.url = url;
    return this;
  }

  /**
   * Build a new instance of the requested API.
   *
   * @return an instance of the API
   * @throws CloudManagerApiException when any error occurs
   */
  public A build() throws CloudManagerApiException {
    if (workspace == null) {
      throw new IllegalStateException("Workspace must be specified.");
    }
    if (workspace.getAuthContext() == null) {
      throw new IllegalStateException("Workspace must specify AuthContext.");
    }
    workspace.getAuthContext().validate();

    try {
      Class impl;
      if (clazz == ContentSetApi.class) {
        impl = ContentSetApiImpl.class;
      } else if (clazz == EnvironmentApi.class) {
        impl = EnvironmentApiImpl.class;
      } else if (clazz == PipelineApi.class) {
        impl = PipelineApiImpl.class;
      } else if (clazz == PipelineExecutionApi.class) {
        impl = PipelineExecutionApiImpl.class;
      } else if (clazz == ProgramApi.class) {
        impl = ProgramApiImpl.class;
      } else if (clazz == RepositoryApi.class) {
        impl = RepositoryApiImpl.class;
      } else if (clazz == TenantApi.class) {
        impl = TenantApiImpl.class;
      } else {
        throw new CloudManagerApiException(String.format("Unknown API requested (%s).", clazz));
      }
      return (A) impl.getDeclaredConstructor(Workspace.class, URL.class).newInstance(workspace, url);
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
      // How did this happen?
      throw new RuntimeException(ex);
    }
  }
}

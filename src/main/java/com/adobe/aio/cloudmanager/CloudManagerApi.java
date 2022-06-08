package com.adobe.aio.cloudmanager;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import com.adobe.aio.cloudmanager.feign.CloudManagerApiImpl;
import com.adobe.aio.cloudmanager.feign.ProgramApiImpl;
import com.adobe.aio.cloudmanager.feign.ProgramExceptionDecoder;
import com.adobe.aio.feign.AIOHeaderInterceptor;
import com.adobe.aio.ims.feign.JWTAuthInterceptor;
import com.adobe.aio.workspace.Workspace;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.slf4j.Slf4jLogger;
import lombok.NonNull;

public interface CloudManagerApi {

  String BASE_URL = "https://cloudmanager.adobe.io";
  String META_SCOPE = "https://ims-na1.adobelogin.com/s/ent_cloudmgr_sdk";

  /**
   * Creates a new CloudManagerApi instance using the specified Workspace.
   *
   * The workspace will be used for authentication and thus must pass a {@link Workspace#validateJwtCredentialConfig()}
   *
   * @param workspace the AIO Workspace Context.
   * @return an api instance
   */
  @NonNull
  static CloudManagerApi create(@NonNull Workspace workspace) {
    try {
      return create(workspace, new URL(BASE_URL));
    } catch (MalformedURLException ex) {
      // How did this happen?
      throw new IllegalStateException(ex.getMessage());
    }
  }

  /**
   * Creates a new CloudManagerApi instance using the specified Workspace and API base URL.
   *
   * The workspace will be used for authentication and thus must pass a {@link Workspace#validateJwtCredentialConfig()}
   *
   * This can be used to override the default Cloud Manager API endpoint.
   *
   * @param workspace  the AIO Workspace Context.
   * @param url the base URL for Cloud Manager's API
   * @return an api instance
   */
  @NonNull
  static CloudManagerApi create(@NonNull Workspace workspace, @NonNull URL url) {
    workspace.getMetascopes().add(META_SCOPE);
    workspace.validateJwtCredentialConfig();

    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    mapper.registerModule(new JavaTimeModule());

    
    Collection<RequestInterceptor> interceptors = new ArrayList<>();
    interceptors.add(JWTAuthInterceptor.builder().workspace(workspace).build());
    interceptors.add(AIOHeaderInterceptor.builder().workspace(workspace).build());

    Feign.Builder builder = Feign.builder()
        .requestInterceptors(interceptors)
        .decoder(new JacksonDecoder(mapper))
        .logger(new Slf4jLogger())
        .logLevel(Logger.Level.FULL);
    
    ProgramApiImpl programApi = builder.errorDecoder(new ProgramExceptionDecoder()).target(ProgramApiImpl.class, url.toString());
    return new CloudManagerApiImpl(url.toString(), programApi);
  }
  
  /**
   * List all programs in the organization
   *
   * @return a list of {@link Program}s
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Programs/getPrograms">List Programs API</a>
   */
  @NonNull
  Collection<Program> listPrograms() throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param programId the id of the program to delete.
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Programs/deleteProgram">Delete Program API</a>
   */
  void deleteProgram(@NonNull String programId) throws CloudManagerApiException;

  /**
   * Delete the specified program.
   *
   * @param program the program to delete
   * @throws CloudManagerApiException when any error occurs
   * @see <a href="https://www.adobe.io/apis/experiencecloud/cloud-manager/api-reference.html#/Programs/deleteProgram">Delete Program API</a>
   */
  void deleteProgram(@NonNull Program program) throws CloudManagerApiException;
}

package com.adobe.aio.cloudmanager.impl.program;

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

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Constants;
import com.adobe.aio.cloudmanager.Program;
import com.adobe.aio.cloudmanager.Region;
import com.adobe.aio.workspace.Workspace;
import feign.Param;
import feign.RequestLine;
import com.adobe.aio.cloudmanager.ProgramApi;
import com.adobe.aio.cloudmanager.Tenant;
import com.adobe.aio.cloudmanager.impl.FeignUtil;
import com.adobe.aio.cloudmanager.impl.generated.EmbeddedProgram;
import com.adobe.aio.cloudmanager.impl.generated.ProgramList;
import com.adobe.aio.cloudmanager.impl.generated.RegionsList;

public class ProgramApiImpl implements ProgramApi {

  private final FeignApi api;

  public ProgramApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? Constants.CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Program get(String programId) throws CloudManagerApiException {
    EmbeddedProgram program = api.get(programId);
    return new ProgramImpl(program, this);
  }

  @Override
  public void delete(String programId) throws CloudManagerApiException {
    api.delete(programId);
  }

  @Override
  public void delete(Program program) throws CloudManagerApiException {
    delete(program.getId());
  }

  @Override
  public Collection<Program> list(String tenantId) throws CloudManagerApiException {
    ProgramList list = api.list(tenantId);
    return list.getEmbedded() == null || list.getEmbedded().getPrograms() == null ?
        Collections.emptyList() :
        list.getEmbedded().getPrograms().stream().map(p -> new ProgramImpl(p, this)).collect(Collectors.toList());
  }

  public Collection<Program> list(Tenant tenant) throws CloudManagerApiException {
    return list(tenant.getId());
  }

  @Override
  public Collection<Region> listRegions(String programId) throws CloudManagerApiException {
    RegionsList list = api.listRegions(programId);
    return list.getEmbedded() == null || list.getEmbedded().getRegions() == null ?
        Collections.emptySet() :
        list.getEmbedded().getRegions().stream().map(r -> Region.fromValue(r.getName())).collect(Collectors.toList());
  }

  private interface FeignApi {
    @RequestLine("GET /api/program/{id}")
    EmbeddedProgram get(@Param("id") String id) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{id}")
    void delete(@Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET /api/tenant/{tenantId}/programs")
    ProgramList list(@Param("tenantId") String tenantId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{id}/regions")
    RegionsList listRegions(@Param("id") String id) throws CloudManagerApiException;
  }
}

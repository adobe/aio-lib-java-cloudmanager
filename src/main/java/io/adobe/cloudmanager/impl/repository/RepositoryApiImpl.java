package io.adobe.cloudmanager.impl.repository;

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

import com.adobe.aio.workspace.Workspace;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Program;
import io.adobe.cloudmanager.Repository;
import io.adobe.cloudmanager.RepositoryApi;
import io.adobe.cloudmanager.impl.FeignUtil;
import io.adobe.cloudmanager.impl.generated.BranchList;
import io.adobe.cloudmanager.impl.generated.RepositoryBranch;
import io.adobe.cloudmanager.impl.generated.RepositoryList;

import static io.adobe.cloudmanager.Constants.*;

public class RepositoryApiImpl implements RepositoryApi {

  private final FeignApi api;

  public RepositoryApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Collection<Repository> list(String programId) throws CloudManagerApiException {
    RepositoryList list = api.list(programId);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getRepositories().stream().map(r -> new RepositoryImpl(r, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<Repository> list(Program program) throws CloudManagerApiException {
    return list(program.getId());
  }

  @Override
  public Collection<Repository> list(String programId, int limit) throws CloudManagerApiException {
    return list(programId, 0, limit);
  }

  @Override
  public Collection<Repository> list(Program program, int limit) throws CloudManagerApiException {
    return list(program.getId(), limit);
  }

  @Override
  public Collection<Repository> list(String programId, int start, int limit) throws CloudManagerApiException {
    RepositoryList list = api.list(programId, start, limit);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getRepositories().stream().map(r -> new RepositoryImpl(r, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<Repository> list(Program program, int start, int limit) throws CloudManagerApiException {
    return list(program.getId(), start, limit);
  }

  @Override
  public Repository get(String programId, String repositoryId) throws CloudManagerApiException {
    return new RepositoryImpl(api.get(programId, repositoryId), this);
  }

  @Override
  public Repository get(Program program, String repositoryId) throws CloudManagerApiException {
    return get(program.getId(), repositoryId);
  }

  @Override
  public Collection<String> listBranches(Repository repository) throws CloudManagerApiException {
    BranchList list = api.listBranches(repository.getProgramId(), repository.getId());
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getBranches().stream().map(RepositoryBranch::getName).collect(Collectors.toList());
  }

  private interface FeignApi {

    @RequestLine("GET /api/program/{programId}/repositories")
    RepositoryList list(@Param("programId") String programId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/repositories?start={start}&limit={limit}")
    RepositoryList list(@Param("programId") String programId, @Param("start") int start, @Param("limit") int limit) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/repository/{id}")
    io.adobe.cloudmanager.impl.generated.Repository get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/repository/{id}/branches")
    BranchList listBranches(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;
  }
}

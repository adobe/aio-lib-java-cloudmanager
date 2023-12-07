package com.adobe.aio.cloudmanager.impl.environment;

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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.adobe.aio.cloudmanager.Region;
import com.adobe.aio.cloudmanager.impl.generated.Environment;
import com.adobe.aio.cloudmanager.impl.generated.EnvironmentLog;
import com.adobe.aio.cloudmanager.impl.generated.RegionDeployment;
import com.adobe.aio.cloudmanager.impl.generated.Variable;
import com.adobe.aio.workspace.Workspace;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.EnvironmentApi;
import com.adobe.aio.cloudmanager.LogOption;
import com.adobe.aio.cloudmanager.impl.FeignUtil;
import com.adobe.aio.cloudmanager.impl.VariableImpl;
import com.adobe.aio.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import com.adobe.aio.cloudmanager.impl.generated.EnvironmentList;
import com.adobe.aio.cloudmanager.impl.generated.EnvironmentLogs;
import com.adobe.aio.cloudmanager.impl.generated.Redirect;
import com.adobe.aio.cloudmanager.impl.generated.RegionDeploymentList;
import com.adobe.aio.cloudmanager.impl.generated.VariableList;

import static com.adobe.aio.cloudmanager.Constants.*;

public class EnvironmentApiImpl implements EnvironmentApi {
  private static final String ENVIRONMENT_LOG_REDIRECT_ERROR = "Log redirect for environment %s, service '%s', log name '%s', date '%s' did not exist.";

  private final FeignApi api;
  private final String baseUrl;

  public EnvironmentApiImpl(Workspace workspace, URL url) {
    baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Collection<com.adobe.aio.cloudmanager.Environment> list(String programId) throws CloudManagerApiException {
    EnvironmentList list = api.list(programId);
    return list.getEmbedded() == null || list.getEmbedded().getEnvironments() == null ?
        Collections.emptyList() :
        list.getEmbedded().getEnvironments().stream().map(e -> new EnvironmentImpl(e, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<com.adobe.aio.cloudmanager.Environment> list(String programId, com.adobe.aio.cloudmanager.Environment.Type type) throws CloudManagerApiException {
    EnvironmentList list = api.list(programId, type.name().toLowerCase());
    return list.getEmbedded() == null || list.getEmbedded().getEnvironments() == null ?
        Collections.emptyList() :
        list.getEmbedded().getEnvironments().stream().map(e -> new EnvironmentImpl(e, this)).collect(Collectors.toList());
  }

  @Override
  public com.adobe.aio.cloudmanager.Environment create(String programId, String name, com.adobe.aio.cloudmanager.Environment.Type type, String region, String description) throws CloudManagerApiException {
    try {
      StringWriter writer = new StringWriter();
      JsonFactory jsonFactory = new JsonFactory();
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      gen.writeStartObject();
      gen.writeStringField("name", name);
      gen.writeStringField("type", type.name().toLowerCase());
      gen.writeStringField("region", region);
      if (StringUtils.isNotBlank(description)) {
        gen.writeStringField("description", description);
      }
      gen.writeEndObject();
      gen.close();
      String body = writer.toString();
      return new EnvironmentImpl(api.create(programId, body), this);
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format(CloudManagerExceptionDecoder.GENERATE_BODY, e.getLocalizedMessage()));
    }
  }

  @Override
  public com.adobe.aio.cloudmanager.Environment get(String programId, String environmentId) throws CloudManagerApiException {
    return new EnvironmentImpl(api.get(programId, environmentId), this);
  }

  @Override
  public void delete(String programId, String environmentId) throws CloudManagerApiException {
    delete(programId, environmentId, false);
  }

  @Override
  public void delete(com.adobe.aio.cloudmanager.Environment environment) throws CloudManagerApiException {
    delete(environment, false);
  }

  @Override
  public void delete(String programId, String environmentId, boolean ignoreFailure) throws CloudManagerApiException {
    api.delete(programId, environmentId, ignoreFailure);
  }

  @Override
  public void delete(com.adobe.aio.cloudmanager.Environment environment, boolean ignoreFailure) throws CloudManagerApiException {
    delete(environment.getProgramId(), environment.getId(), ignoreFailure);
  }

  @Override
  public Collection<com.adobe.aio.cloudmanager.EnvironmentLog> listLogs(String programId, String environmentId, LogOption option, int days) throws CloudManagerApiException {
    EnvironmentLogs list = api.listLogs(programId, environmentId, option.getService(), option.getName(), days);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getDownloads().stream().map(EnvironmentLogImpl::new).collect(Collectors.toList());
  }

  @Override
  public Collection<com.adobe.aio.cloudmanager.EnvironmentLog> listLogs(com.adobe.aio.cloudmanager.Environment environment, LogOption option, int days) throws CloudManagerApiException {
    return listLogs(environment.getProgramId(), environment.getId(), option, days);
  }

  @Override
  public String getLogDownloadUrl(String programId, String environmentId, LogOption option, LocalDate date) throws CloudManagerApiException {
    Redirect redirect = api.getLogs(programId, environmentId, option.getService(), option.getName(), date.toString());
    if (redirect != null && StringUtils.isNotBlank(redirect.getRedirect())) {
      return redirect.getRedirect();
    }
    throw new CloudManagerApiException(String.format(ENVIRONMENT_LOG_REDIRECT_ERROR, environmentId, option.getService(), option.getName(), date));
  }

  @Override
  public String getLogDownloadUrl(com.adobe.aio.cloudmanager.Environment environment, LogOption option, LocalDate date) throws CloudManagerApiException {
    return getLogDownloadUrl(environment.getProgramId(), environment.getId(), option, date);
  }

  @Override
  public com.adobe.aio.cloudmanager.RegionDeployment getRegionDeployment(String programId, String environmentId, String deploymentId) throws CloudManagerApiException {
    return new RegionDeploymentImpl(api.getDeployment(programId, environmentId, deploymentId));
  }

  @Override
  public Collection<com.adobe.aio.cloudmanager.RegionDeployment> listRegionDeployments(String programId, String environmentId) throws CloudManagerApiException {
    RegionDeploymentList list = api.listDeployments(programId, environmentId);
    return list.getEmbedded() == null || list.getEmbedded().getRegionDeployments() == null ?
        Collections.emptyList() :
        list.getEmbedded().getRegionDeployments().stream().map(rd -> new RegionDeploymentImpl(rd)).collect(Collectors.toList());
  }

  @Override
  public Collection<com.adobe.aio.cloudmanager.RegionDeployment> listRegionDeployments(com.adobe.aio.cloudmanager.Environment environment) throws CloudManagerApiException {
    return listRegionDeployments(environment.getProgramId(), environment.getId());
  }

  @Override
  public void createRegionDeployments(com.adobe.aio.cloudmanager.Environment environment, Region... regions) throws CloudManagerApiException {
    List<String> list = Arrays.stream(regions).map(Region::getValue).collect(Collectors.toList());
    api.addDeployments(environment.getProgramId(), environment.getId(), list);
  }

  @Override
  public void removeRegionDeployments(com.adobe.aio.cloudmanager.Environment environment, Region... regions) throws CloudManagerApiException {
    RegionDeploymentList list = api.listDeployments(environment.getProgramId(), environment.getId());
    List<RegionDeployment> toRemove = new ArrayList<>(regions.length);

    for (Region region : regions) {
      RegionDeployment deployment =
          list.getEmbedded().getRegionDeployments()
              .stream()
              .filter(r -> r.getRegion().equals(region.getValue()))
              .findFirst()
              .orElseThrow(() -> new CloudManagerApiException(String.format("Cannot remove region deployment, Environment %s is not deployed to region '%s'.", environment.getId(), region.getValue())));
      toRemove.add(deployment);
    }
    toRemove.forEach(d -> d.setStatus(RegionDeployment.StatusEnum.TO_DELETE));
    api.removeDeployments(environment.getProgramId(), environment.getId(), toRemove);
  }

  @Override
  public Set<com.adobe.aio.cloudmanager.Variable> getVariables(String programId, String environmentId) throws CloudManagerApiException {
    VariableList list = api.getVariables(programId, environmentId);
    return list.getEmbedded() == null || list.getEmbedded().getVariables() == null ?
        Collections.emptySet() :
        list.getEmbedded().getVariables().stream().map(VariableImpl::new).collect(Collectors.toSet());
  }

  @Override
  public Set<com.adobe.aio.cloudmanager.Variable> getVariables(com.adobe.aio.cloudmanager.Environment environment) throws CloudManagerApiException {
    return getVariables(environment.getProgramId(), environment.getId());
  }

  @Override
  public Set<com.adobe.aio.cloudmanager.Variable> setVariables(String programId, String environmentId, com.adobe.aio.cloudmanager.Variable... variables) throws
      CloudManagerApiException {
    List<Variable> toSet =
        Arrays.stream(variables).map((v) -> new Variable()
                .name(v.getName())
                .value(v.getValue())
                .type(Variable.TypeEnum.fromValue(v.getVarType().getValue()))
                .service(v.getTier().name().toLowerCase()))
            .collect(Collectors.toList());
    VariableList list = api.setVariables(programId, environmentId, toSet);
    return list.getEmbedded() == null || list.getEmbedded().getVariables() == null ?
        Collections.emptySet() : // How did creating a variable result in none returned?
        list.getEmbedded().getVariables().stream().map(VariableImpl::new).collect(Collectors.toSet());
  }

  @Override
  public Set<com.adobe.aio.cloudmanager.Variable> setVariables(com.adobe.aio.cloudmanager.Environment environment, com.adobe.aio.cloudmanager.Variable... variables) throws CloudManagerApiException {
    return setVariables(environment.getProgramId(), environment.getId(), variables);
  }

  @Override
  public void resetRde(String programId, String environmentId) throws CloudManagerApiException {
    api.reset(programId, environmentId);
  }

  @Override
  public void resetRde(com.adobe.aio.cloudmanager.Environment environment) throws CloudManagerApiException {
    resetRde(environment.getProgramId(), environment.getId());
  }

  @Override
  public Optional<com.adobe.aio.cloudmanager.Environment> get(String programId, Predicate<com.adobe.aio.cloudmanager.Environment> predicate) throws CloudManagerApiException {
    return list(programId).stream().filter(predicate).findFirst();
  }

  @Override
  public Collection<com.adobe.aio.cloudmanager.EnvironmentLog> downloadLogs(String programId, String environmentId, LogOption logOption, int days, File dir) throws CloudManagerApiException {
    EnvironmentLogs logs = api.listLogs(programId, environmentId, logOption.getService(), logOption.getName(), days);
    if (logs.getEmbedded() == null || logs.getEmbedded().getDownloads() == null || logs.getEmbedded().getDownloads().isEmpty()) {
      return Collections.emptyList();
    }
    List<EnvironmentLog> downloads = logs.getEmbedded().getDownloads();
    List<com.adobe.aio.cloudmanager.EnvironmentLog> downloaded = new ArrayList<>();
    for (EnvironmentLog log : downloads) {
      String logfileName = String.format("environment-%s-%s-%s-%s.log.gz", environmentId, log.getService(), log.getName(), log.getDate());
      EnvironmentLogImpl impl = new EnvironmentLogImpl(log, String.format("%s/%s", dir.getPath(), logfileName));
      downloadLog(impl);
      downloaded.add(impl);
    }
    return downloaded;
  }

  @Override
  public Collection<com.adobe.aio.cloudmanager.EnvironmentLog> downloadLogs(com.adobe.aio.cloudmanager.Environment environment, LogOption logOption, int days, File dir) throws CloudManagerApiException {
    return downloadLogs(environment.getProgramId(), environment.getId(), logOption, days, dir);
  }

  private void downloadLog(EnvironmentLogImpl log) throws CloudManagerApiException {
    Redirect redirect = api.getLogs(log.getProgramId(), log.getEnvironmentId(), log.getService(), log.getName(), log.getDate().toString());
    try {
      File downloaded = new File(log.getDownloadPath());
      FileUtils.copyInputStreamToFile(new URL(redirect.getRedirect()).openStream(), downloaded);
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format("Cannot download %s%s to %s (Cause: %s).", baseUrl, log.getUrl(), log.getDownloadPath(), e.getClass().getName()));
    }
  }

  private interface FeignApi {

    @RequestLine("GET /api/program/{programId}/environments")
    EnvironmentList list(@Param("programId") String programId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environments?type={type}")
    EnvironmentList list(@Param("programId") String programId, @Param("type") String type) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/environments")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    Environment create(@Param("programId") String programId, @Param("body") String body) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{id}")
    Environment get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/environment/{id}?ignoreResourcesDeletionResult={ignore}")
    Environment delete(@Param("programId") String programId, @Param("id") String id, @Param("ignore") boolean ignore) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{environmentId}/logs?service={service}&name={name}&days={days}")
    EnvironmentLogs listLogs(@Param("programId") String programId, @Param("environmentId") String environmentId, @Param("service") String service, @Param("name") String name, @Param("days") int days) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{environmentId}/logs/download?service={service}&name={name}&date={date}")
    Redirect getLogs(@Param("programId") String programId, @Param("environmentId") String environmentId, @Param("service") String service, @Param("name") String name, @Param("date") String date) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{environmentId}/regionDeployments/{id}")
    RegionDeployment getDeployment(@Param("programId") String programId, @Param("environmentId") String environmentId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{environmentId}/regionDeployments")
    RegionDeploymentList listDeployments(@Param("programId") String programId, @Param("environmentId") String environmentId) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/environment/{environmentId}/regionDeployments")
    @Headers("Content-Type: application/json")
    RegionDeploymentList addDeployments(@Param("programId") String programId, @Param("environmentId") String environmentId, List<String> regions) throws CloudManagerApiException;

    @RequestLine("PATCH /api/program/{programId}/environment/{environmentId}/regionDeployments")
    @Headers("Content-Type: application/json")
    RegionDeploymentList removeDeployments(@Param("programId") String programId, @Param("environmentId") String environmentId, List<RegionDeployment> deployments) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{id}/variables")
    VariableList getVariables(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PATCH /api/program/{programId}/environment/{id}/variables")
    @Headers("Content-Type: application/json")
    VariableList setVariables(@Param("programId") String programId, @Param("id") String id, List<Variable> variables) throws CloudManagerApiException;

    @RequestLine("PUT /api/program/{programId}/environment/{id}/reset")
    void reset(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;
  }
}

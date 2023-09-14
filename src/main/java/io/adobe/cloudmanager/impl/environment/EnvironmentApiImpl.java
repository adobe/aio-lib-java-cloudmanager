package io.adobe.cloudmanager.impl.environment;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.adobe.aio.workspace.Workspace;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Environment;
import io.adobe.cloudmanager.EnvironmentApi;
import io.adobe.cloudmanager.EnvironmentLog;
import io.adobe.cloudmanager.LogOption;
import io.adobe.cloudmanager.RegionDeployment;
import io.adobe.cloudmanager.Variable;
import io.adobe.cloudmanager.impl.FeignUtil;
import io.adobe.cloudmanager.impl.VariableImpl;
import io.adobe.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import io.adobe.cloudmanager.impl.generated.EnvironmentList;
import io.adobe.cloudmanager.impl.generated.EnvironmentLogs;
import io.adobe.cloudmanager.impl.generated.Redirect;
import io.adobe.cloudmanager.impl.generated.VariableList;

import static io.adobe.cloudmanager.Constants.*;

public class EnvironmentApiImpl implements EnvironmentApi {
  private static final String ENVIRONMENT_LOG_REDIRECT_ERROR = "Log redirect for environment %s, service '%s', log name '%s', date '%s' did not exist.";

  private final FeignApi api;

  public EnvironmentApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
  }

  @Override
  public Collection<Environment> list(String programId) throws CloudManagerApiException {
    EnvironmentList list = api.list(programId);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getEnvironments().stream().map(e -> new EnvironmentImpl(e, this)).collect(Collectors.toList());
  }

  @Override
  public Collection<Environment> list(String programId, Environment.Type type) throws CloudManagerApiException {
    EnvironmentList list = api.list(programId, type.name().toLowerCase());
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getEnvironments().stream().map(e -> new EnvironmentImpl(e, this)).collect(Collectors.toList());
  }

  @Override
  public Environment create(String programId, String name, Environment.Type type, String region, String description) throws CloudManagerApiException {
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
  public Environment get(String programId, String environmentId) throws CloudManagerApiException {
    return new EnvironmentImpl(api.get(programId, environmentId), this);
  }

  @Override
  public void delete(String programId, String environmentId) throws CloudManagerApiException {
    delete(programId, environmentId, false);
  }

  @Override
  public void delete(Environment environment) throws CloudManagerApiException {
    delete(environment, false);
  }

  @Override
  public void delete(String programId, String environmentId, boolean ignoreFailure) throws CloudManagerApiException {
    api.delete(programId, environmentId, ignoreFailure);
  }

  @Override
  public void delete(Environment environment, boolean ignoreFailure) throws CloudManagerApiException {
    delete(environment.getProgramId(), environment.getId(), ignoreFailure);
  }

  @Override
  public Collection<EnvironmentLog> listLogs(String programId, String environmentId, LogOption option, int days) throws CloudManagerApiException {
    EnvironmentLogs list = api.listLogs(programId, environmentId, option.getService(), option.getName(), days);
    return list.getEmbedded() == null ?
        Collections.emptyList() :
        list.getEmbedded().getDownloads().stream().map(EnvironmentLogImpl::new).collect(Collectors.toList());
  }

  @Override
  public Collection<EnvironmentLog> listLogs(Environment environment, LogOption option, int days) throws CloudManagerApiException {
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
  public String getLogDownloadUrl(Environment environment, LogOption option, LocalDate date) throws CloudManagerApiException {
    return getLogDownloadUrl(environment.getProgramId(), environment.getId(), option, date);
  }

  @Override
  public RegionDeployment getRegionDeployment(String programId, String environmentId, String deploymentId) throws CloudManagerApiException {
    return new RegionDeploymentImpl(api.getDeployment(programId, environmentId, deploymentId), this);
  }

  @Override
  public Collection<RegionDeployment> listRegionDeployments(String programId, String environmentId) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Collection<RegionDeployment> listRegionDeployments(Environment environment) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Collection<RegionDeployment> createRegionDeployment(Environment environment, String region) throws CloudManagerApiException {
    return null;
  }


  @Override
  public Collection<RegionDeployment> removeRegionDeployment(Environment environment, String region) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Collection<RegionDeployment> removeRegionDeployment(RegionDeployment deployment) throws CloudManagerApiException {
    return null;
  }

  @Override
  public Set<Variable> getVariables(String programId, String environmentId) throws CloudManagerApiException {
    VariableList list = api.getVariables(programId, environmentId);
    return list.getEmbedded() == null ?
        Collections.emptySet() :
        list.getEmbedded().getVariables().stream().map(VariableImpl::new).collect(Collectors.toSet());
  }

  @Override
  public Set<Variable> getVariables(Environment environment) throws CloudManagerApiException {
    return getVariables(environment.getProgramId(), environment.getId());
  }

  @Override
  public Set<Variable> setVariables(String programId, String environmentId, Variable... variables) throws CloudManagerApiException {
    List<io.adobe.cloudmanager.impl.generated.Variable> toSet =
        Arrays.stream(variables).map((v) -> new io.adobe.cloudmanager.impl.generated.Variable()
                .name(v.getName())
                .value(v.getValue())
                .type(io.adobe.cloudmanager.impl.generated.Variable.TypeEnum.fromValue(v.getVarType().getValue()))
                .service(v.getService()))
            .collect(Collectors.toList());
    VariableList list = api.setVariables(programId, environmentId, toSet);
    return list.getEmbedded() == null ?
        Collections.emptySet() :
        list.getEmbedded().getVariables().stream().map(VariableImpl::new).collect(Collectors.toSet());
  }

  @Override
  public Set<Variable> setVariables(Environment environment, Variable... variables) throws CloudManagerApiException {
    return setVariables(environment.getProgramId(), environment.getId(), variables);
  }

  private interface FeignApi {

    @RequestLine("GET /api/program/{programId}/environments")
    EnvironmentList list(@Param("programId") String programId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environments?type={type}")
    EnvironmentList list(@Param("programId") String programId, @Param("type") String type) throws CloudManagerApiException;

    @RequestLine("POST /api/program/{programId}/environments")
    @Headers("Content-Type: application/json")
    @Body("{body}")
    io.adobe.cloudmanager.impl.generated.Environment create(@Param("programId") String programId, @Param("body") String body) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{id}")
    io.adobe.cloudmanager.impl.generated.Environment get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/environment/{id}?ignoreResourcesDeletionResult={ignore}")
    io.adobe.cloudmanager.impl.generated.Environment delete(@Param("programId") String programId, @Param("id") String id, @Param("ignore") boolean ignore) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{environmentId}/logs?service={service}&name={name}&days={days}")
    EnvironmentLogs listLogs(@Param("programId") String programId, @Param("environmentId") String environmentId, @Param("service") String service, @Param("name") String name, @Param("days") int days) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{environmentId}/logs/download?service={service}&name={name}&date={date}")
    Redirect getLogs(@Param("programId") String programId, @Param("environmentId") String environmentId, @Param("service") String service, @Param("name") String name, @Param("date") String date) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/environment/{environmentId}/regionDeployments/{id}")
    io.adobe.cloudmanager.impl.generated.RegionDeployment getDeployment(@Param("programId") String programId, @Param("environmentId") String environmentId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET api/program/{programId}/environment/{id}/variables")
    VariableList getVariables(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PATCH api/program/{programId}/environment/{id}/variables")
    @Headers("Content-Type: application/json")
    VariableList setVariables(@Param("programId") String programId, @Param("id") String id, List<io.adobe.cloudmanager.impl.generated.Variable> variables) throws CloudManagerApiException;
  }
}

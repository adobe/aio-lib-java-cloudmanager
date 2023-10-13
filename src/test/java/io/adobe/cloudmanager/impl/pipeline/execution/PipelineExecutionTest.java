package io.adobe.cloudmanager.impl.pipeline.execution;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.adobe.aio.ims.feign.AuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.Artifact;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineApi;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionApi;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import io.adobe.cloudmanager.StepAction;
import io.adobe.cloudmanager.impl.AbstractApiTest;
import io.adobe.cloudmanager.impl.pipeline.PipelineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;
import org.mockserver.verify.VerificationTimes;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class PipelineExecutionTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("pipeline/execution/get.json");
  private static final JsonBody GET_WAITING_BODY = loadBodyJson("pipeline/execution/approval-waiting.json");
  private static final JsonBody GET_CODE_QUALITY_BODY = loadBodyJson("pipeline/execution/codeQuality-waiting.json");
  public static final JsonBody LIST_BODY = loadBodyJson("pipeline/execution/list.json");

  private PipelineApi pipelineApi;
  private PipelineExecutionApiImpl executionApi;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      pipelineApi = PipelineApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
      executionApi = (PipelineExecutionApiImpl) PipelineExecutionApi.builder().workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  private HttpRequest setupDownloadUrl(String sessionId, String path) {
    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath(path);
    client.when(redirect).respond(
        response()
            .withStatusCode(OK_200.code())
            .withBody(json(String.format("{ \"redirect\": \"%s/logs/special.txt\" }", baseUrl)))
    );
    return redirect;
  }

  private HttpRequest setupDownloadUrlSpecial(String sessionId, String path) {
    HttpRequest redirect = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath(path)
        .withQueryStringParameter("file", "somethingspecial");
    client.when(redirect).respond(
        response()
            .withStatusCode(OK_200.code())
            .withBody(String.format("{ \"redirect\": \"%s/logs/somethingspecial.txt\" }", baseUrl))
    );
    return redirect;
  }

  @Test
  void current_failure_404() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    assertFalse(executionApi.getCurrent("1", "1").isPresent(), "Correct state");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void current_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));

    Optional<PipelineExecution> opt = executionApi.getCurrent("1", "1");

    assertTrue(opt.isPresent(), "Execution found.");
    PipelineExecution exec = opt.get();
    assertEquals("1", exec.getId(), "Id Matches");
    assertEquals("1", exec.getProgramId(), "Program Id Matches");
    assertEquals("1", exec.getPipelineId(), "Pipeline Id Matches");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void current_success_via_pipeline(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));

    Pipeline pipeline = new PipelineImpl(mock, pipelineApi, executionApi);
    Optional<PipelineExecution> opt = pipeline.getCurrentExecution();

    assertTrue(opt.isPresent(), "Execution found.");
    PipelineExecution exec = opt.get();
    assertEquals("1", exec.getId(), "Id Matches");
    assertEquals("1", exec.getProgramId(), "Program Id Matches");
    assertEquals("1", exec.getPipelineId(), "Pipeline Id Matches");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void start_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.start("1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot create execution: %s/api/program/1/pipeline/1/execution (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(put);
    client.clear(put);
  }

  @Test
  void start_failure_412() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(PRECONDITION_FAILED_412.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.start("1", "1"), "Exception thrown");
    assertEquals("Cannot create execution. Pipeline already running.", exception.getMessage(), "Message was correct");
    client.verify(put);
    client.clear(put);
  }

  @Test
  void start_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(CREATED_201.code()).withBody(GET_BODY));

    PipelineExecution execution = executionApi.start("1", "1");
    assertNotNull(execution);
    client.verify(put);
    client.clear(put);
  }

  @Test
  void start_success_via_pipeline(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest put = request().withMethod("PUT").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(put).respond(response().withStatusCode(CREATED_201.code()).withBody(GET_BODY));

    Pipeline p = new PipelineImpl(mock, pipelineApi, executionApi);
    PipelineExecution execution = p.startExecution();
    assertNotNull(execution);
    client.verify(put);
    client.clear(put);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.get("1", "1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot get execution: %s/api/program/1/pipeline/1/execution/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecution exec = executionApi.get("1", "1", "1");
    assertNotNull(exec);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success_via_pipeline(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecution exec = new PipelineImpl(mock, pipelineApi, executionApi).getExecution("1");
    assertNotNull(exec);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getStepState_failure_nostep(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getStepState(new PipelineExecutionImpl(mock, executionApi), StepAction.deploy), "Exception thrown.");
    assertEquals("Cannot find step state for action 'deploy' on execution 1.", exception.getMessage(), "Message was correct");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getStepState_success(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    PipelineExecutionStepState stepState = executionApi.getStepState(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality);
    assertNotNull(stepState);
    assertEquals(PipelineExecutionStepState.Status.NOT_STARTED, stepState.getStatusState());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getStepState_viaExecution() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withBody(LIST_BODY));

    final List<PipelineExecution> executions = new ArrayList<>(executionApi.list("1", "1"));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executions.get(0).getStep(StepAction.deploy));
    assertEquals("Cannot find step with action 'deploy' for pipeline 1, execution 1.", exception.getMessage(), "Message was correct.");

    exception = assertThrows(CloudManagerApiException.class, () -> executions.get(19).getStep(StepAction.build), "Exception thrown.");
    assertEquals("Cannot find step with action 'build' for pipeline 1, execution 20.", exception.getMessage(), "Message was correct.");

    PipelineExecution execution = executions.get(0);
    assertEquals("build",  execution.getStep(StepAction.build).getAction(), "Correct step found.");

    client.verify(list, VerificationTimes.once());
    client.clear(list);
  }

  @Test
  void getCurrentStep(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withBody(LIST_BODY));

    List<PipelineExecution> executions = new ArrayList<>(executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi)));
    PipelineExecution execution = executions.get(0);
    assertEquals("build",  execution.getCurrentStep().getAction(), "Correct step found.");

    // No running step.
    execution = executions.get(1);
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, execution::getCurrentStep, "Exception thrown.");
    assertEquals("Cannot find a current step for pipeline 1, execution 2.", exception.getMessage(), "Message was correct.");

    // No steps
    execution = executions.get(19);
    exception = assertThrows(CloudManagerApiException.class, execution::getCurrentStep, "Exception thrown.");
    assertEquals("Cannot find a current step for pipeline 1, execution 20.", exception.getMessage(), "Message was correct.");

    client.verify(list, VerificationTimes.once());
    client.clear(list);
  }

  @Test
  void advance_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance")
        .withBody(new JsonBody("{ \"approved\": true }"));
    client.when(put).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.advance("1", "1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot advance execution: %s/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void advance_failure_buildRunning() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.advance("1", "1", "1"), "Exception thrown");
    assertEquals("Cannot find a waiting step for pipeline 1, execution 1.", exception.getMessage(), "Message was correct");

    client.verify(get);
    client.clear(get);
  }

  @Test
  void advance_approval_waiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance")
        .withBody(new JsonBody("{ \"approved\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.advance("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void advance_codeQuality_waiting(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));

    HttpRequest metrics = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(metrics).respond(response().withBody(loadBodyJson("pipeline/execution/codeQuality-metrics.json")));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/advance")
        .withBody(loadBodyJson("pipeline/execution/put-metrics-override.json"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.advance(new PipelineExecutionImpl(mock, executionApi));
    client.verify(get, metrics, put);
    client.clear(get);
    client.clear(metrics);
    client.clear(put);
  }

  @Test
  void advance_via_execution() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    ObjectMapper objectMapper = new ObjectMapper();

    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream("pipeline/execution/approval-waiting.json")) {
      io.adobe.cloudmanager.impl.generated.PipelineExecution original = objectMapper.readValue(is, io.adobe.cloudmanager.impl.generated.PipelineExecution.class);

      HttpRequest put = request().withMethod("PUT")
          .withHeader(API_KEY_HEADER, sessionId)
          .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance")
          .withBody(new JsonBody("{ \"approved\": true }"));
      client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

      new PipelineExecutionImpl(original, executionApi).advance();
      client.verify(put);
      client.clear(put);
    }
  }

  @Test
  void cancel_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel")
        .withBody(new JsonBody("{ \"approved\": false }"));
    client.when(put).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.cancel("1", "1", "1"), "Exception thrown");
    assertEquals(String.format("Cannot cancel execution: %s/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_failure_nostep() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/execution/no-active.json")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.cancel("1", "1", "1"), "Exception thrown");
    assertEquals("Cannot find a cancelable step for pipeline 1, execution 1.", exception.getMessage(), "Message was correct");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void cancel_build_running() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/cancel")
        .withBody(json("{ \"cancel\": true }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_approval_waiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_WAITING_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel")
        .withBody(new JsonBody("{ \"approved\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_codeQuality_waiting(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_CODE_QUALITY_BODY));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/cancel")
        .withBody(json("{ \"override\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel(new PipelineExecutionImpl(mock, executionApi));
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_deploy_waiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/execution/deploy-waiting.json")));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/2/cancel")
        .withBody(json("{ \"resume\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    executionApi.cancel("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_via_execution() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    ObjectMapper objectMapper = new ObjectMapper();

    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream("pipeline/execution/approval-waiting.json")) {
      io.adobe.cloudmanager.impl.generated.PipelineExecution original = objectMapper.readValue(is, io.adobe.cloudmanager.impl.generated.PipelineExecution.class);

      HttpRequest put = request().withMethod("PUT")
          .withHeader(API_KEY_HEADER, sessionId)
          .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel")
          .withBody(new JsonBody("{ \"approved\": false }"));
      client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

      new PipelineExecutionImpl(original, executionApi).cancel();
      client.verify(put);
      client.clear(put);
    }
  }

  @Test
  void getStepLogDownloadUrl_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(redirect).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getStepLogDownloadUrl("1", "1", "1", StepAction.build), "Exception thrown");
    assertEquals(String.format("Cannot get logs: %s/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getStepLogDownloadUrl_no_redirect() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/logs");
    client.when(redirect).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getStepLogDownloadUrl("1", "1", "1", StepAction.validate), "Exception thrown");
    assertEquals("Log redirect for execution 1, action 'validate' did not exist.", exception.getMessage(), "Message was correct.");
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getStepLogDownloadUrl_success(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = setupDownloadUrl(sessionId, "/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");

    PipelineExecution execution = new PipelineExecutionImpl(mock, executionApi);
    assertEquals(String.format("%s/logs/special.txt", baseUrl), executionApi.getStepLogDownloadUrl(execution, StepAction.build));
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getStepLogDownloadUrl_success_alternateFile(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = setupDownloadUrlSpecial(sessionId, "/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    PipelineExecution execution = new PipelineExecutionImpl(mock, executionApi);

    assertEquals(String.format("%s/logs/somethingspecial.txt", baseUrl), executionApi.getStepLogDownloadUrl(execution, StepAction.build, "somethingspecial"));
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getMetrics_failure_403(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest exec = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(exec).respond(response().withBody(GET_BODY));

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics");
    client.when(get).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getQualityGateResults(new PipelineExecutionImpl(mock, executionApi), StepAction.codeQuality), "Exception thrown.");
    assertEquals(String.format("Cannot get metrics: %s/api/program/1/pipeline/1/execution/1/phase/2/step/2/metrics (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");

    client.verify(exec, get);
    client.clear(exec);
    client.clear(get);
  }

  @Test
  void list_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.list("1", "1"), "Exception thrown.");
    assertEquals(String.format("Cannot list executions: %s/api/program/1/pipeline/1/executions (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi));
    assertEquals(20, executions.size(), "Correct length.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_limit_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.list("1", "1", 10), "Exception thrown.");
    assertEquals(String.format("Cannot list executions: %s/api/program/1/pipeline/1/executions?start=0&limit=10 (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_limit_success(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "30");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 30);
    assertEquals(20, executions.size(), "Correct length.");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_start_limit_success(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/executions")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "30");
    client.when(list).respond(response().withBody(LIST_BODY));
    Collection<PipelineExecution> executions = executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi), 10, 30);
    assertEquals(20, executions.size(), "Correct length.");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void listArtifacts_failure_403(@Mock PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifacts");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.listArtifacts(step), "Exception thrown.");
    assertEquals(String.format("Cannot list step artifacts: %s/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifacts (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listArtifacts_success(@Mock PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifacts");
    client.when(list).respond(response().withBody(loadBodyJson("pipeline/execution/list-artifacts.json")));
    Collection<Artifact> artifacts = executionApi.listArtifacts(step);
    assertEquals(1, artifacts.size(), "Collection size correct.");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void getArtifactDownloadUrl_failure_404(@Mock PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifact/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getArtifactDownloadUrl(step, "1"), "Exception thrown.");
    assertEquals(String.format("Cannot get step artifact: %s/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifact/1 (404 Not Found).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getArtifactDownloadUrl_no_redirect(@Mock PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifact/1");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> executionApi.getArtifactDownloadUrl(step, "1"), "Exception thrown");
    assertEquals("Artifact redirect for execution 1, phase 1, step 1 did not exist.", exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getArtifactDownloadUrl_success(@Mock PipelineExecution execution, @Mock PipelineExecutionStepState step) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(execution.getProgramId()).thenReturn("1");
    when(execution.getPipelineId()).thenReturn("1");
    when(execution.getId()).thenReturn("1");
    when(step.getExecution()).thenReturn(execution);
    when(step.getPhaseId()).thenReturn("1");
    when(step.getStepId()).thenReturn("1");
    HttpRequest get = setupDownloadUrl(sessionId, "/api/program/1/pipeline/1/execution/1/phase/1/step/1/artifact/1");
    client.when(get).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));
    String redirect = executionApi.getArtifactDownloadUrl(step, "1");
    assertEquals(String.format("%s/logs/special.txt", baseUrl), redirect, "Response was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void isRunning(@Mock io.adobe.cloudmanager.impl.generated.Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/executions");
    client.when(list).respond(response().withBody(LIST_BODY));

    List<PipelineExecution> executions = new ArrayList<>(executionApi.list(new PipelineImpl(mock, pipelineApi, executionApi)));
    assertTrue(executions.get(0).isRunning());
    assertFalse(executions.get(1).isRunning());
    client.verify(list, VerificationTimes.once());
    client.clear(list);
  }

  @Test
  void downloadStepLog_redirect_failure_404() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest getRedirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/build.txt\" }", baseUrl))));

    HttpRequest getFile = request().withMethod("GET").withPath("/logs/build.txt");
    client.when(getFile).respond(response().withStatusCode(NOT_FOUND_404.code()));

    final PipelineExecution exec = executionApi.get("1", "1", "1");
    final PipelineExecutionStepState step = exec.getStep(StepAction.build);
    final File outputDir = Files.createTempDirectory("log-output").toFile();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> step.getLog(outputDir), "Exception thrown.");
    assertEquals(String.format("Cannot download log for pipeline 1, execution 1, step 'build' to %s/pipeline-1-execution-1-build.txt (Cause: java.io.FileNotFoundException).", outputDir, baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get);
    client.verify(getRedirect);
    client.verify(getFile);
    client.clear(get);
    client.clear(getRedirect);
    client.clear(getFile);
  }

  @Test
  void downloadStepLog_success() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest getRedirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/build.txt\" }", baseUrl))));

    HttpRequest getFile = request().withMethod("GET").withPath("/logs/build.txt");
    client.when(getFile).respond(response().withBody("some log line\nsome other log line\n"));

    final PipelineExecution exec = executionApi.get("1", "1", "1");
    final PipelineExecutionStepState step = exec.getStep(StepAction.build);
    final File outputDir = Files.createTempDirectory("log-output").toFile();

    step.getLog(outputDir);
    assertTrue(FileUtils.sizeOf(new File(outputDir, "pipeline-1-execution-1-build.txt")) > 0, "File is not empty.");

    client.verify(get);
    client.verify(getRedirect);
    client.verify(getFile);
    client.clear(get);
    client.clear(getRedirect);
    client.clear(getFile);
  }


  @Test
  void downloadStepLog_namedFile_redirect_failure_404() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest getRedirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs").withQueryStringParameter("file", "named");
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/build-special.txt\" }", baseUrl))));

    HttpRequest getFile = request().withMethod("GET").withPath("/logs/build-special.txt");
    client.when(getFile).respond(response().withStatusCode(NOT_FOUND_404.code()));

    final PipelineExecution exec = executionApi.get("1", "1", "1");
    final PipelineExecutionStepState step = exec.getStep(StepAction.build);
    final File outputDir = Files.createTempDirectory("log-output").toFile();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> step.getLog("named", outputDir), "Exception thrown.");
    assertEquals(String.format("Cannot download 'named' log for pipeline 1, execution 1, step 'build' to %s/pipeline-1-execution-1-build-named.txt (Cause: java.io.FileNotFoundException).", outputDir, baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get);
    client.verify(getRedirect);
    client.verify(getFile);
    client.clear(get);
    client.clear(getRedirect);
    client.clear(getFile);
  }

  @Test
  void downloadStepLog_named_success() throws CloudManagerApiException, IOException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest getRedirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs").withQueryStringParameter("file", "named");
    client.when(getRedirect).respond(response().withBody(json(String.format("{ \"redirect\": \"%s/logs/build.txt\" }", baseUrl))));

    HttpRequest getFile = request().withMethod("GET").withPath("/logs/build.txt");
    client.when(getFile).respond(response().withBody("some log line\nsome other log line\n"));

    final PipelineExecution exec = executionApi.get("1", "1", "1");
    final PipelineExecutionStepState step = exec.getStep(StepAction.build);
    final File outputDir = Files.createTempDirectory("log-output").toFile();

    step.getLog("named", outputDir);
    assertTrue(FileUtils.sizeOf(new File(outputDir, "pipeline-1-execution-1-build-named.txt")) > 0, "File is not empty.");

    client.verify(get);
    client.verify(getRedirect);
    client.verify(getFile);
    client.clear(get);
    client.clear(getRedirect);
    client.clear(getFile);
  }
}

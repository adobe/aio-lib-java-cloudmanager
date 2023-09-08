package io.adobe.cloudmanager.impl;

import java.io.InputStream;
import java.nio.channels.Pipe;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Pipeline;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import io.adobe.cloudmanager.StepAction;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class PipelineExecutionTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("execution/get.json");
  private static final JsonBody GET_WAITING_BODY = loadBodyJson("execution/approval-waiting.json");
  private static final JsonBody GET_CODE_QUALITY_BODY = loadBodyJson("execution/codeQuality-waiting.json");
  public static final JsonBody LIST_BODY = loadBodyJson("execution/list.json");

  private HttpRequest setupDownloadUrl(String sessionId) {
    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(redirect).respond(
        response()
            .withStatusCode(OK_200.code())
            .withBody(json(String.format("{ \"redirect\": \"%s/logs/special.txt\" }", baseUrl)))
    );
    return redirect;
  }


  private HttpRequest setupDownloadUrlSpecial(String sessionId) {
    HttpRequest redirect = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs")
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

    assertFalse(underTest.getCurrentExecution("1", "1").isPresent(), "Correct state");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void current_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution");
    client.when(get).respond(response().withBody(GET_BODY));

    Optional<PipelineExecution> opt = underTest.getCurrentExecution("1", "1");

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

    Pipeline pipeline = new PipelineImpl(mock, underTest);
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("1", "1"), "Exception thrown");
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startExecution("1", "1"), "Exception thrown");
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

    PipelineExecution execution = underTest.startExecution("1", "1");
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

    Pipeline p = new PipelineImpl(mock, underTest);
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecution("1", "1", "1"), "Exception thrown");
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

    PipelineExecution exec = underTest.getExecution("1", "1", "1");
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

    PipelineExecution exec = new PipelineImpl(mock, underTest).getExecution("1");
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepState(new PipelineExecutionImpl(mock, underTest), StepAction.deploy), "Exception thrown.");
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

    PipelineExecutionStepState stepState = underTest.getExecutionStepState(new PipelineExecutionImpl(mock, underTest), StepAction.codeQuality);
    assertNotNull(stepState);
    assertEquals(PipelineExecutionStepState.Status.NOT_STARTED, stepState.getStatusState());
    client.verify(get);
    client.clear(get);
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.advanceExecution("1", "1", "1"), "Exception thrown");
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.advanceExecution("1", "1", "1"), "Exception thrown");
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

    underTest.advanceExecution("1", "1", "1");
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
    client.when(metrics).respond(response().withBody(loadBodyJson("execution/codeQuality-metrics.json")));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/2/advance")
        .withBody(loadBodyJson("execution/put-metrics-override.json"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    underTest.advanceExecution(new PipelineExecutionImpl(mock, underTest));
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

    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream("execution/approval-waiting.json")) {
      io.adobe.cloudmanager.impl.generated.PipelineExecution original = objectMapper.readValue(is, io.adobe.cloudmanager.impl.generated.PipelineExecution.class);

      HttpRequest put = request().withMethod("PUT")
          .withHeader(API_KEY_HEADER, sessionId)
          .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/advance")
          .withBody(new JsonBody("{ \"approved\": true }"));
      client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

      new PipelineExecutionImpl(original, underTest).advance();
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution("1", "1", "1"), "Exception thrown");
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
    client.when(get).respond(response().withBody(loadBodyJson("execution/no-active.json")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelExecution("1", "1", "1"), "Exception thrown");
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

    underTest.cancelExecution("1", "1", "1");
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

    underTest.cancelExecution("1", "1", "1");
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

    underTest.cancelExecution(new PipelineExecutionImpl(mock, underTest));
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }


  @Test
  void cancel_deploy_waiting() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(loadBodyJson("execution/deploy-waiting.json")));

    HttpRequest put = request().withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/2/cancel")
        .withBody(json("{ \"resume\": false }"));
    client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

    underTest.cancelExecution("1", "1", "1");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void cancel_via_execution() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    ObjectMapper objectMapper = new ObjectMapper();

    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream("execution/approval-waiting.json")) {
      io.adobe.cloudmanager.impl.generated.PipelineExecution original = objectMapper.readValue(is, io.adobe.cloudmanager.impl.generated.PipelineExecution.class);

      HttpRequest put = request().withMethod("PUT")
          .withHeader(API_KEY_HEADER, sessionId)
          .withPath("/api/program/1/pipeline/1/execution/1/phase/4/step/3/cancel")
          .withBody(new JsonBody("{ \"approved\": false }"));
      client.when(put).respond(response().withStatusCode(ACCEPTED_202.code()));

      new PipelineExecutionImpl(original, underTest).cancel();
      client.verify(put);
      client.clear(put);
    }
  }

  @Test
  void getExecutionStepLogDownloadUrl_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs");
    client.when(redirect).respond(response().withStatusCode(FORBIDDEN_403.code()));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl("1", "1", "1", StepAction.build), "Exception thrown");
    assertEquals(String.format("Cannot get logs: %s/api/program/1/pipeline/1/execution/1/phase/2/step/1/logs (403 Forbidden).", baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }


  @Test
  void getExecutionStepLogDownloadUrl_no_redirect() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1/phase/1/step/1/logs");
    client.when(redirect).respond(response().withStatusCode(OK_200.code()).withBody(json("{}")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getExecutionStepLogDownloadUrl("1", "1", "1", StepAction.validate), "Exception thrown");
    assertEquals("Log redirect for execution 1, action 'validate' did not exist.", exception.getMessage(), "Message was correct.");
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }


  @Test
  void getExecutionStepLogDownloadUrl_success(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = setupDownloadUrl(sessionId);

    PipelineExecution execution =  new PipelineExecutionImpl(mock, underTest);
    assertEquals(String.format("%s/logs/special.txt", baseUrl), underTest.getExecutionStepLogDownloadUrl(execution, StepAction.build));
    client.verify(get, redirect);
    client.clear(get);
    client.clear(redirect);
  }

  @Test
  void getExecutionStepLogDownloadUrl_success_alternateFile(@Mock io.adobe.cloudmanager.impl.generated.PipelineExecution mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getPipelineId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/execution/1");
    client.when(get).respond(response().withBody(GET_BODY));
    HttpRequest redirect = setupDownloadUrlSpecial(sessionId);
    PipelineExecution execution =  new PipelineExecutionImpl(mock, underTest);

    assertEquals(String.format("%s/logs/somethingspecial.txt", baseUrl), underTest.getExecutionStepLogDownloadUrl(execution, StepAction.build, "somethingspecial"));
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

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getQualityGateResults(new PipelineExecutionImpl(mock, underTest), StepAction.codeQuality), "Exception thrown.");
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
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listExecutions("1", "1"), "Exception thrown.");
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

    Collection<PipelineExecution> executions = underTest.listExecutions(new PipelineImpl(mock, underTest));
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
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listExecutions("1", "1", 10), "Exception thrown.");
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
    Collection<PipelineExecution> executions = underTest.listExecutions(new PipelineImpl(mock, underTest), 30);
    assertEquals(20, executions.size(), "Correct length.");

    client.verify(list);
    client.clear(list);
  }

}

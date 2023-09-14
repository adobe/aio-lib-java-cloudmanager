package io.adobe.cloudmanager.impl.pipeline.execution;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2021 Adobe Inc.
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.Metric;
import io.adobe.cloudmanager.PipelineExecution;
import io.adobe.cloudmanager.PipelineExecutionApi;
import io.adobe.cloudmanager.PipelineExecutionStepState;
import io.adobe.cloudmanager.StepAction;
import io.adobe.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class PipelineExecutionStepStateImpl extends io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState implements PipelineExecutionStepState {

  private static final long serialVersionUID = 1L;

  public static final String ACTION_APPROVAL = "approval";
  public static final String ACTION_SCHEDULE = "schedule";
  public static final String ACTION_DEPLOY = "deploy";


  @Delegate
  private final io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionApi client;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecution execution;

  public PipelineExecutionStepStateImpl(io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState delegate,
                                        PipelineExecution execution,
                                        PipelineExecutionApi client) {
    this.delegate = delegate;
    this.execution = execution;
    this.client = client;
  }

  @Override
  public Status getStatusState() {
    return Status.fromValue(getStatus().getValue());
  }

  @Override
  public PipelineExecution getExecution() throws CloudManagerApiException {
    return execution;
  }

  @Override
  public boolean hasLogs() {
    return delegate.getLinks().getHttpnsAdobeComadobecloudrelpipelinelogs() != null;
  }

  @Override
  public void getLog(OutputStream outputStream) throws CloudManagerApiException {
//    client.downloadExecutionStepLog(this, null, outputStream);
  }

  @Override
  public void getLog(String name, OutputStream outputStream) throws CloudManagerApiException {
//    client.downloadExecutionStepLog(this, name, outputStream);
  }

  protected String getAdvanceBody() throws CloudManagerApiException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    try {
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      gen.writeStartObject();
      if (ACTION_APPROVAL.equals(getAction())) {
        gen.writeBooleanField("approved", true);
      } else {
        gen.writeFieldName("metrics");
        gen.writeStartArray();
        buildMetricsOverride(gen);
        gen.writeEndArray();
      }
      gen.writeEndObject();
      gen.close();
      return writer.toString();
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format(CloudManagerExceptionDecoder.GENERATE_BODY, e.getLocalizedMessage()));
    }
  }

  public String getCancelBody() throws CloudManagerApiException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();

    try {
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      gen.writeStartObject();
      if (ACTION_APPROVAL.equals(getAction())) {
        gen.writeBooleanField("approved", false);
      } else if (io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState.StatusEnum.WAITING.equals(getStatus()) &&
          !ACTION_SCHEDULE.equals(getAction()) && !ACTION_DEPLOY.equals(getAction())) {
        gen.writeBooleanField("override", false);
      } else if (io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState.StatusEnum.WAITING.equals(getStatus()) &&
          ACTION_DEPLOY.equals(getAction())) {
        gen.writeBooleanField("resume", false);
      } else {
        gen.writeBooleanField("cancel", true);
      }
      gen.writeEndObject();
      gen.close();
      return writer.toString();
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format(CloudManagerExceptionDecoder.GENERATE_BODY, e.getLocalizedMessage()));
    }
  }


  /**
   * Predicate for pipelines based on they are the current execution.
   */
  public static final Predicate<io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState> IS_CURRENT = (stepState ->
      stepState.getStatus() != io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState.StatusEnum.FINISHED
  );

  /**
   * Predicate for pipelines that are in a waiting state.
   */
  public static final Predicate<io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState> IS_WAITING = (stepState ->
      stepState.getStatus() == io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState.StatusEnum.WAITING
  );

  /**
   * Predicate for pipelines that are in a waiting state.
   */
  public static final Predicate<io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState> IS_RUNNING = (stepState ->
      stepState.getStatus() == io.adobe.cloudmanager.impl.generated.PipelineExecutionStepState.StatusEnum.RUNNING
  );

  /*
   * Builds the body needed to override any blocking metrics for advancing the pipeline.
   */
  private void buildMetricsOverride(JsonGenerator gen) throws CloudManagerApiException, IOException {
    Collection<Metric> metrics = client.getQualityGateResults(getExecution(), StepAction.codeQuality);
    Collection<Metric> failed = metrics.stream().filter(m -> !m.isPassed() && Metric.Severity.IMPORTANT.equals(m.getSev())).collect(Collectors.toList());
    for (Metric m : failed) {
      gen.writeStartObject();
      gen.writeStringField("kpi", m.getKpi());
      gen.writeBooleanField("override", true);
      gen.writeEndObject();
    }
  }
}

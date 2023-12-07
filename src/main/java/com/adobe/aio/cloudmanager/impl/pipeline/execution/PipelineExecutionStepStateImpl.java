package com.adobe.aio.cloudmanager.impl.pipeline.execution;

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
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.adobe.aio.cloudmanager.impl.generated.PipelineExecutionStepState;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Metric;
import com.adobe.aio.cloudmanager.PipelineExecution;
import com.adobe.aio.cloudmanager.PipelineExecutionApi;
import com.adobe.aio.cloudmanager.StepAction;
import com.adobe.aio.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public class PipelineExecutionStepStateImpl extends PipelineExecutionStepState implements com.adobe.aio.cloudmanager.PipelineExecutionStepState {

  private static final long serialVersionUID = 1L;

  public static final String ACTION_APPROVAL = "approval";
  public static final String ACTION_SCHEDULE = "schedule";
  public static final String ACTION_DEPLOY = "deploy";

  @Delegate
  private final PipelineExecutionStepState delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecutionApi client;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final PipelineExecution execution;

  public PipelineExecutionStepStateImpl(PipelineExecutionStepState delegate,
                                        PipelineExecution execution,
                                        PipelineExecutionApi client) {
    this.delegate = delegate;
    this.execution = execution;
    this.client = client;
  }

  @Override
  public StepAction getStepAction() {
    return StepAction.valueOf(delegate.getAction());
  }

  @Override
  public Status getStatusState() {
    return Status.valueOf(getStatus().getValue());
  }

  @Override
  public PipelineExecution getExecution() {
    return execution;
  }

  @Override
  public boolean hasLogs() {
    return delegate.getLinks().getHttpnsAdobeComadobecloudrelpipelinelogs() != null;
  }

  @Override
  public void getLog(File dir) throws CloudManagerApiException {
    String downloadUrl = client.getStepLogDownloadUrl(execution, getStepAction());
    String filename = String.format("pipeline-%s-execution-%s-%s.txt", execution.getPipelineId(), execution.getId(), getStepAction());

    try {
      File downloaded = new File(dir, filename);
      FileUtils.copyInputStreamToFile(new URL(downloadUrl).openStream(), downloaded);
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format("Cannot download log for pipeline %s, execution %s, step '%s' to %s/%s (Cause: %s).", execution.getPipelineId(), execution.getId(), getStepAction(), dir, filename, e.getClass().getName()));
    }
  }

  @Override
  public void getLog(String name, File dir) throws CloudManagerApiException {
    String downloadUrl = client.getStepLogDownloadUrl(execution, getStepAction(), name);
    String filename = String.format("pipeline-%s-execution-%s-%s-%s.txt", execution.getPipelineId(), execution.getId(), getStepAction(), name);

    try {
      File downloaded = new File(dir, filename);
      FileUtils.copyInputStreamToFile(new URL(downloadUrl).openStream(), downloaded);
    } catch (IOException e) {
      throw new CloudManagerApiException(String.format("Cannot download '%s' log for pipeline %s, execution %s, step '%s' to %s/%s (Cause: %s).", name, execution.getPipelineId(), execution.getId(), getStepAction(), dir, filename, e.getClass().getName()));
    }
  }

  protected String getAdvanceBody() throws CloudManagerApiException {
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    try {
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      gen.writeStartObject();
      if (StepAction.approval == getStepAction()) {
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
      if (StepAction.approval == getStepAction()) {
        gen.writeBooleanField("approved", false);
      } else if (PipelineExecutionStepState.StatusEnum.WAITING.equals(getStatus()) &&
          StepAction.schedule != getStepAction() && StepAction.deploy != getStepAction()) {
        gen.writeBooleanField("override", false);
      } else if (PipelineExecutionStepState.StatusEnum.WAITING.equals(getStatus()) &&
          StepAction.deploy == getStepAction()) {
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

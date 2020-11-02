package io.adobe.cloudmanager.model;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 Adobe Inc.
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
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adobe.cloudmanager.CloudManagerApi;
import io.adobe.cloudmanager.CloudManagerApiException;
import io.adobe.cloudmanager.swagger.model.HalLink;
import io.adobe.cloudmanager.swagger.model.Metric;
import io.adobe.cloudmanager.swagger.model.PipelineStepMetrics;
import io.adobe.cloudmanager.util.Predicates;
import lombok.ToString;
import lombok.experimental.Delegate;
import static io.adobe.cloudmanager.CloudManagerApiException.*;

/**
 * Extension to the Swagger generated Pipeline. Provides convenience methods for frequently used APIs
 */
@ToString
public class PipelineExecution extends io.adobe.cloudmanager.swagger.model.PipelineExecution {

  public static final String ACTION_APPROVAL = "approval";
  public static final String ACTION_SCHEDULE = "schedule";
  public static final String ACTION_DEPLOY = "deploy";
  @Delegate
  private final io.adobe.cloudmanager.swagger.model.PipelineExecution delegate;
  @ToString.Exclude
  private final CloudManagerApi client;

  public PipelineExecution(io.adobe.cloudmanager.swagger.model.PipelineExecution delegate, CloudManagerApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  /**
   * Cancel this execution, if in a valid state.
   *
   * @throws CloudManagerApiException when any error occurs.
   */
  public void cancel() throws CloudManagerApiException {
    client.cancelExecution(this);
  }

  /**
   * Get the url to advance this pipeline.
   *
   * @return the URL to the Advance API endpoint
   * @throws CloudManagerApiException when any error occurs
   */
  public String getAdvanceLink() throws CloudManagerApiException {
    PipelineExecutionStepState step = client.getWaitingStep(this);
    HalLink link = step.getLinks().getHttpnsAdobeComadobecloudrelpipelineadvance();
    if (link == null) {
      throw new CloudManagerApiException(ErrorType.FIND_ADVANCE_LINK, step.getAction());
    }
    return link.getHref();
  }

  /**
   * Build the necessary request body to advance this execution.
   *
   * @return the body to submit to the API
   * @throws CloudManagerApiException when any error occurs
   */
  public String getAdvanceBody() throws CloudManagerApiException {
    PipelineExecutionStepState step = client.getWaitingStep(this);
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();
    try {
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      gen.writeStartObject();
      if (ACTION_APPROVAL.equals(step.getAction())) {
        gen.writeBooleanField("approved", true);
      } else {
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        gen.writeFieldName("metrics");
        gen.writeStartArray();
        buildMetricsOverride(mapper, gen, step);
        gen.writeEndArray();
        gen.writeBooleanField("override", true);
      }
      gen.writeEndObject();
      gen.close();
      return writer.toString();
    } catch (IOException e) {
      throw new CloudManagerApiException(ErrorType.GENERATE_BODY, e.getMessage());
    }
  }

  /**
   * Get the url to cacnel this pipeline.
   *
   * @return the URL to the Cancel API endpoint
   * @throws CloudManagerApiException when any error occurs
   */
  public String getCancelLink() throws CloudManagerApiException {
    PipelineExecutionStepState step = client.getCurrentStep(this);
    HalLink link;

    if (io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState.StatusEnum.WAITING.equals(step.getStatus()) &&
        ACTION_DEPLOY.equals(step.getAction())) {
      link = step.getLinks().getHttpnsAdobeComadobecloudrelpipelineadvance();
    } else {
      link = step.getLinks().getHttpnsAdobeComadobecloudrelpipelinecancel();
    }
    if (link == null) {
      throw new CloudManagerApiException(ErrorType.FIND_CANCEL_LINK, step.getAction());
    }
    return link.getHref();
  }

  /**
   * Build the necessary request body to cancel this execution.
   *
   * @return the body to submit to the API
   * @throws CloudManagerApiException when any error occurs
   */
  public String getCancelBody() throws CloudManagerApiException {
    PipelineExecutionStepState step = client.getCurrentStep(this);
    StringWriter writer = new StringWriter();
    JsonFactory jsonFactory = new JsonFactory();

    try {
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      gen.writeStartObject();
      if (ACTION_APPROVAL.equals(step.getAction())) {
        gen.writeBooleanField("approved", false);
      } else if (io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState.StatusEnum.WAITING.equals(step.getStatus()) &&
          !ACTION_SCHEDULE.equals(step.getAction()) && !ACTION_DEPLOY.equals(step.getAction())) {
        gen.writeBooleanField("override", false);
      } else if (io.adobe.cloudmanager.swagger.model.PipelineExecutionStepState.StatusEnum.WAITING.equals(step.getStatus()) &&
          ACTION_DEPLOY.equals(step.getAction())) {
        gen.writeBooleanField("resume", false);
      } else {
        gen.writeBooleanField("cancel", true);
      }
      gen.writeEndObject();
      gen.close();
      return writer.toString();
    } catch (IOException e) {
      throw new CloudManagerApiException(ErrorType.GENERATE_BODY, e.getMessage());
    }
  }

  /*
   * Builds the body needed to override any blocking metrics for advancing the pipeline.
   */
  private void buildMetricsOverride(ObjectMapper mapper, JsonGenerator gen,
                                    PipelineExecutionStepState step) throws CloudManagerApiException, IOException {
    PipelineStepMetrics metrics = client.getQualityGateResults(step);
    List<Metric> failed = metrics.getMetrics().stream().filter(Predicates.FAILED).collect(Collectors.toList());
    for (Metric m : failed) {
      mapper.writeValue(gen, m);
    }
  }
}

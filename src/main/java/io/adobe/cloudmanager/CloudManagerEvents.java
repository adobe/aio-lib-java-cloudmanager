package io.adobe.cloudmanager;

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

import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.adobe.cloudmanager.generated.events.PipelineExecutionEndEvent;
import io.adobe.cloudmanager.generated.events.PipelineExecutionStartEvent;
import io.adobe.cloudmanager.generated.events.PipelineExecutionStartEventEvent;
import io.adobe.cloudmanager.generated.events.PipelineExecutionStepEndEvent;
import io.adobe.cloudmanager.generated.events.PipelineExecutionStepStartEvent;
import io.adobe.cloudmanager.generated.events.PipelineExecutionStepWaitingEvent;
import io.adobe.cloudmanager.generated.invoker.JSON;

public class CloudManagerEvents {

  public static final String STARTED_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/started";
  public static final String WAITING_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/waiting";
  public static final String ENDED_EVENT_TYPE = "https://ns.adobe.com/experience/cloudmanager/event/ended";
  public static final String PIPELINE_EXECUTION_TYPE = "https://ns.adobe.com/experience/cloudmanager/pipeline-execution";
  public static final String PIPELINE_STEP_STATE_TYPE = "https://ns.adobe.com/experience/cloudmanager/execution-step-state";

  public static final Set<Class<?>> EVENTS = Set.of(
      PipelineExecutionStartEvent.class,
      PipelineExecutionStepStartEvent.class,
      PipelineExecutionStepWaitingEvent.class,
      PipelineExecutionStepEndEvent.class,
      PipelineExecutionEndEvent.class
  );

  /**
   * Attempts to determine which event type from the provided JSON String.
   *
   * @param source event JSON string
   * @return Event class type or null
   */
  @CheckForNull
  public static Class<?> typeFrom(String source) throws CloudManagerApiException {
    try {
      // Any object will do, to get the root of the object tree.
      PipelineExecutionStartEvent tester = new JSON().getContext(PipelineExecutionStartEvent.class).readValue(source, PipelineExecutionStartEvent.class);
      PipelineExecutionStartEventEvent event = tester.getEvent();

      if (PIPELINE_EXECUTION_TYPE.equals(event.getXdmEventEnvelopeobjectType()) && STARTED_EVENT_TYPE.equals(event.getAtType())) {
        return PipelineExecutionStartEvent.class;
      } else if (PIPELINE_EXECUTION_TYPE.equals(event.getXdmEventEnvelopeobjectType()) && ENDED_EVENT_TYPE.equals(event.getAtType())) {
        return PipelineExecutionEndEvent.class;
      } else if (PIPELINE_STEP_STATE_TYPE.equals(event.getXdmEventEnvelopeobjectType()) && STARTED_EVENT_TYPE.equals(event.getAtType())) {
        return PipelineExecutionStepStartEvent.class;
      } else if (PIPELINE_STEP_STATE_TYPE.equals(event.getXdmEventEnvelopeobjectType()) && WAITING_EVENT_TYPE.equals(event.getAtType())) {
        return PipelineExecutionStepWaitingEvent.class;
      } else if (PIPELINE_STEP_STATE_TYPE.equals(event.getXdmEventEnvelopeobjectType()) && ENDED_EVENT_TYPE.equals(event.getAtType())) {
        return PipelineExecutionStepEndEvent.class;
      }
      return null;
    } catch (JsonProcessingException e) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.PROCESS_EVENT, e.getMessage());
    }
  }

  /**
   * Attempts to convert the provided string source into the specified event type T.
   * <p>
   * T must be one of:
   * - {@link PipelineExecutionStartEvent}
   * - {@link PipelineExecutionStepStartEvent}
   * - {@link PipelineExecutionStepWaitingEvent}
   * - {@link PipelineExecutionStepEndEvent}
   * - {@link PipelineExecutionEndEvent}
   *
   * @param source String representation of the event
   * @param type   class type of event
   * @param <T>    the type of event
   * @return a fully populated event of the specified type
   */
  @Nonnull
  public static <T> T parseEvent(String source, Class<T> type) throws CloudManagerApiException {

    if (!EVENTS.contains(type)) {
      throw new IllegalArgumentException(String.format("Unknown event type: %s", type));
    }
    try {
      return new JSON().getContext(type).readValue(source, type);
    } catch (JsonProcessingException e) {
      throw new CloudManagerApiException(CloudManagerApiException.ErrorType.PROCESS_EVENT, e.getMessage());
    }
  }
}

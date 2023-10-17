package io.adobe.cloudmanager;

import java.time.OffsetDateTime;

public interface PipelineExecutionEvent {
  /**
   * The id of this event.
   *
   * @return the id
   */
  String getId();

  /**
   * The time at which the event was published.
   *
   * @return the event published time
   */
  OffsetDateTime getPublished();
}

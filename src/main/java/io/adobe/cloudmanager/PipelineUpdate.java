package io.adobe.cloudmanager;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class PipelineUpdate {

    String branch;
    String repositoryId;

}

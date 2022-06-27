package com.adobe.aio.cloudmanager.feign;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2022 Adobe Inc.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.PipelineExecution;
import com.adobe.aio.cloudmanager.impl.model.PipelineExecutionListRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineExecutionIterator implements Iterator<PipelineExecution> {
  private static final Logger logger = LoggerFactory.getLogger(PipelineExecutionIterator.class);


  private final List<com.adobe.aio.cloudmanager.impl.model.PipelineExecution> executions = new ArrayList<>();
  private final CloudManagerApiImpl client;
  private PipelineExecutionListRepresentation list;
  private int position = 0;

  PipelineExecutionIterator(PipelineExecutionListRepresentation list, CloudManagerApiImpl client) {
    this.list = list;
    this.client = client;
    executions.addAll(list.getEmbedded().getExecutions());
  }

  @Override
  public boolean hasNext() {
    checkForMore();
    return position < executions.size();
  }

  @Override
  public PipelineExecution next() {
    PipelineExecution next = new PipelineExecutionImpl(executions.get(position), client);
    position++;
    return next;
  }

  private void checkForMore() {
    if (executions.size() == position) {
      try {
        list = client.getNextPage(list);
        if (list.getEmbedded() != null && list.getEmbedded().getExecutions() != null && !list.getEmbedded().getExecutions().isEmpty()) {
          executions.addAll(list.getEmbedded().getExecutions());
        }
      } catch (CloudManagerApiException e) {
        logger.error(e.getLocalizedMessage());
      }
    }
  }
}

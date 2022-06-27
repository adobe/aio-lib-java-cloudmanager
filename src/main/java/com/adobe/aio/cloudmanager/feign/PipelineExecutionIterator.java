package com.adobe.aio.cloudmanager.feign;

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

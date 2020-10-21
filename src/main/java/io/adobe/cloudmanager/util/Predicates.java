package io.adobe.cloudmanager.util;

import io.adobe.cloudmanager.model.Pipeline;

import java.util.function.Predicate;

public class Predicates {

  public static final Predicate<Pipeline> IS_BUSY = (pipeline -> io.adobe.cloudmanager.swagger.model.Pipeline.StatusEnum.BUSY == pipeline.getStatus());
}

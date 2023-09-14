package io.adobe.cloudmanager.impl;

import io.adobe.cloudmanager.Variable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class VariableImpl extends io.adobe.cloudmanager.impl.generated.Variable implements Variable {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final io.adobe.cloudmanager.impl.generated.Variable delegate;

  public VariableImpl(io.adobe.cloudmanager.impl.generated.Variable delegate) {
    this.delegate = delegate;
  }

  @Override
  public Type getVarType() {
    return Type.fromValue(delegate.getType().getValue());
  }
}

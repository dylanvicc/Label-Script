package com.ls.api.engine.ast.node.impl;

import java.util.List;

public class ConcatExpression extends ExpressionNode {

  private final List<ExpressionNode> parts;

  public ConcatExpression(List<ExpressionNode> parts) {
    this.parts = parts;
  }

  public List<ExpressionNode> getParts() {
    return parts;
  }
}

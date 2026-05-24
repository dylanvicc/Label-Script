package com.ls.api.engine.ast.node.impl;

public class VariableExpression extends ExpressionNode {

  private final String path;

  public VariableExpression(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}

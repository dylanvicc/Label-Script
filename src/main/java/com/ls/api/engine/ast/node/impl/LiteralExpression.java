package com.ls.api.engine.ast.node.impl;

public class LiteralExpression extends ExpressionNode {

  private final String value;

  public LiteralExpression(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

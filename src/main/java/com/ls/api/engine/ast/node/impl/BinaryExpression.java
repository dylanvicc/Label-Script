package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.BinaryOperator;

public class BinaryExpression extends ExpressionNode {

  private final ExpressionNode left;
  private final BinaryOperator operator;
  private final ExpressionNode right;

  public BinaryExpression(ExpressionNode left, BinaryOperator operator, ExpressionNode right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public ExpressionNode getLeft() {
    return left;
  }

  public BinaryOperator getOperator() {
    return operator;
  }

  public ExpressionNode getRight() {
    return right;
  }
}

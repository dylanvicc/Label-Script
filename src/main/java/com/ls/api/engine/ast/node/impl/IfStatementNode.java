package com.ls.api.engine.ast.node.impl;

import java.util.List;

public class IfStatementNode extends StatementNode {

  private final ExpressionNode condition;
  private final List<StatementNode> thenBranch;
  private final List<StatementNode> elseBranch;

  public IfStatementNode(ExpressionNode condition, List<StatementNode> thenBranch, List<StatementNode> elseBranch) {
    this.condition = condition;
    this.thenBranch = thenBranch;
    this.elseBranch = elseBranch;
  }

  public ExpressionNode getCondition() {
    return condition;
  }

  public List<StatementNode> getThenBranch() {
    return thenBranch;
  }

  public List<StatementNode> getElseBranch() {
    return elseBranch;
  }
}

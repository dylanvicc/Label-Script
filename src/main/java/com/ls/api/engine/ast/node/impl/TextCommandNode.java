package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.Position;

public class TextCommandNode extends CommandNode {

  private final Position position;
  private final ExpressionNode text;

  public TextCommandNode(Position position, ExpressionNode text) {
    this.position = position;
    this.text = text;
  }

  public Position getPosition() {
    return position;
  }

  public ExpressionNode getText() {
    return text;
  }
}

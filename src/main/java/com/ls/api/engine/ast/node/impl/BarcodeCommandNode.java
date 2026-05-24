package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.BarcodeType;
import com.ls.api.engine.ast.Position;

public class BarcodeCommandNode extends CommandNode {

  private final BarcodeType type;
  private final Position position;
  private final ExpressionNode value;
  private final int height;
  private final boolean humanReadable;

  public BarcodeCommandNode(BarcodeType type, Position position, ExpressionNode value, int height, boolean humanReadable) {
    this.type = type;
    this.position = position;
    this.value = value;
    this.height = height;
    this.humanReadable = humanReadable;
  }

  public BarcodeType getType() {
    return type;
  }

  public Position getPosition() {
    return position;
  }

  public ExpressionNode getValue() {
    return value;
  }

  public int getHeight() {
    return height;
  }

  public boolean isHumanReadable() {
    return humanReadable;
  }
}

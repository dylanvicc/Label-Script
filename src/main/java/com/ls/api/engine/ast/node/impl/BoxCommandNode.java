package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.Position;
import com.ls.api.engine.ast.Size;

public class BoxCommandNode extends CommandNode {

  private final Position position;
  private final Size size;
  private final int borderThickness;

  public BoxCommandNode(Position position, Size size, int borderThickness) {
    this.position = position;
    this.size = size;
    this.borderThickness = borderThickness;
  }

  public Position getPosition() {
    return position;
  }

  public Size getSize() {
    return size;
  }

  public int getBorderThickness() {
    return borderThickness;
  }
}

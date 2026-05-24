package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.Position;
import com.ls.api.engine.ast.Size;

public class ReverseCommandNode extends CommandNode {

  private final Position position;
  private final Size size;

  public ReverseCommandNode(Position position, Size size) {
    this.position = position;
    this.size = size;
  }

  public Position getPosition() {
    return position;
  }

  public Size getSize() {
    return size;
  }
}

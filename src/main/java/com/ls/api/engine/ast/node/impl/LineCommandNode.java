package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.Position;

public class LineCommandNode extends CommandNode {

  private final Position start;
  private final Position end;
  private final int thickness;

  public LineCommandNode(Position start, Position end, int thickness) {
    this.start = start;
    this.end = end;
    this.thickness = thickness;
  }

  public Position getStart() {
    return start;
  }

  public Position getEnd() {
    return end;
  }

  public int getThickness() {
    return thickness;
  }
}

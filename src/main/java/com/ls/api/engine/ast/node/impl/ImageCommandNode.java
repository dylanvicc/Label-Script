package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.Position;

public class ImageCommandNode extends CommandNode {

  private final Position position;
  private final String image;

  public ImageCommandNode(Position position, String image) {
    this.position = position;
    this.image = image;
  }

  public Position getPosition() {
    return position;
  }

  public String getImageName() {
    return image;
  }
}

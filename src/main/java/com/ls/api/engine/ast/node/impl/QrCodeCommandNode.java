package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.Position;

public class QrCodeCommandNode extends CommandNode {

  private final Position position;
  private final ExpressionNode value;
  private final int moduleSize;
  private final int errorCorrectionLevel;

  public QrCodeCommandNode(Position position, ExpressionNode value, int moduleSize, int errorCorrectionLevel) {
    this.position = position;
    this.value = value;
    this.moduleSize = moduleSize;
    this.errorCorrectionLevel = errorCorrectionLevel;
  }

  public Position getPosition() {
    return position;
  }

  public ExpressionNode getValue() {
    return value;
  }

  public int getModuleSize() {
    return moduleSize;
  }

  public int getErrorCorrectionLevel() {
    return errorCorrectionLevel;
  }
}

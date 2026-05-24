package com.ls.api.engine.ast.node.impl;

import com.ls.api.engine.ast.FontSpec;
import com.ls.api.engine.ast.Position;

public class TextBlockCommandNode extends CommandNode {

  private final Position position;
  private final ExpressionNode text;
  private final int width;
  private final int maxLines;
  private final String alignment;
  private final FontSpec font;

  public TextBlockCommandNode(Position position, ExpressionNode text, int width, int maxLines, String alignment, FontSpec font) {
    this.position = position;
    this.text = text;
    this.width = width;
    this.maxLines = maxLines;
    this.alignment = alignment;
    this.font = font;
  }

  public Position getPosition() {
    return position;
  }

  public ExpressionNode getText() {
    return text;
  }

  public int getWidth() {
    return width;
  }

  public int getMaxLines() {
    return maxLines;
  }

  public String getAlignment() {
    return alignment;
  }

  public FontSpec getFont() {
    return font;
  }
}

package com.ls.api.engine.ast;

public class FontSpec {

  private final String font;
  private final int height;
  private final int width;
  private final int rotation;

  public FontSpec(String font, int height, int width, int rotation) {
    this.font = font;
    this.height = height;
    this.width = width;
    this.rotation = rotation;
  }

  public String getFontName() {
    return font;
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public int getRotation() {
    return rotation;
  }
}

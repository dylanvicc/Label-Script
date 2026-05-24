package com.ls.api.engine.transpile.zpl;

public class ZplAssembler {

  /**
   * A mutable sequence of characters. Appends label content.
   */
  private final StringBuilder builder = new StringBuilder();

  /**
   * Appends the character sequence to introduce a new label.
   */
  public void beginLabel() {
    builder.append("^XA\n");
  }

  /**
   * Appends the character sequence to terminate a label.
   */
  public void endLabel() {
    builder.append("^XZ");
  }

  /**
   * Draws a line of text.
   * 
   * @param x    The X coordinate of the character sequence.
   * @param y    The Y coordinate of the character sequence.
   * @param text The text to draw.
   */
  public void drawText(int x, int y, String text) {
    builder.append("^FO").append(x).append(",").append(y).append("^ADN,36,20^FD").append(escape(text)).append("^FS\n");
  }

  /**
   * Draws a rectangle.
   * 
   * @param x         The X coordinate of the rectangle.
   * @param y         The Y coordinate of the rectangle.
   * @param width     The width of the rectangle.
   * @param height    The height of the rectangle.
   * @param thickness The thickness of the border lines.
   */
  public void drawBox(int x, int y, int width, int height, int thickness) {
    builder.append("^FO").append(x).append(",").append(y).append("^GB").append(width).append(",").append(height)
        .append(",").append(thickness).append(",B,0^FS\n");
  }

  /**
   * Draws a bar-code that can represent 128 ASCII characters.
   * 
   * @param x             The X coordinate of the bar-code.
   * @param y             The Y coordinate of the bar-code.
   * @param value         The serialized value.
   * @param height        The height of the bar-code.
   * @param humanReadable Denotes if a human readable translation is appended.
   */
  public void drawBarcodeCode128(int x, int y, String value, int height, boolean humanReadable) {
    builder.append("^FO").append(x).append(",").append(y).append("^BCN,").append(height).append(",")
        .append(humanReadable ? "Y" : "N").append(",N,N^FD").append(escape(value)).append("^FS\n");
  }

  /**
   * Draws a bar-code that can represent 39 characters.
   * 
   * @param x             The X coordinate of the bar-code.
   * @param y             The Y coordinate of the bar-code.
   * @param value         The serialized value.
   * @param height        The height of the bar-code.
   * @param humanReadable Denotes if a human readable translation is appended.
   */
  public void drawBarcodeCode39(int x, int y, String value, int height, boolean humanReadable) {
    builder.append("^FO").append(x).append(",").append(y).append("^B3N,N,").append(height).append(",")
        .append(humanReadable ? "Y" : "N").append(",N^FD").append(escape(value)).append("^FS\n");
  }

  /**
   * Draws an EAN-13 bar-code.
   * 
   * @param x             The X coordinate of the bar-code.
   * @param y             The Y coordinate of the bar-code.
   * @param value         The serialized value.
   * @param height        The height of the bar-code.
   * @param humanReadable Denotes if a human readable translation is appended.
   */
  public void drawBarcodeEan13(int x, int y, String value, int height, boolean humanReadable) {
    builder.append("^FO").append(x).append(",").append(y).append("^BEN,").append(height).append(",")
        .append(humanReadable ? "Y" : "N").append("^FD").append(escape(value)).append("^FS\n");
  }

  /**
   * Draws a QR code.
   * 
   * @param x          The X coordinate of the QR code.
   * @param y          The Y coordinate of the QR code.
   * @param value      The serialized value.
   * @param moduleSize The size of each QR module.
   * @param level      The error correction level.
   */
  public void drawQrCode(int x, int y, String value, int moduleSize, int level) {
    final String eccLetter = switch (level) {
    case 1 -> "L";
    case 3 -> "Q";
    case 4 -> "H";
    default -> "M";
    };
    builder.append("^FO").append(x).append(",").append(y).append("^BQN,2,").append(moduleSize).append("^FD")
        .append(eccLetter).append("A,").append(escape(value)).append("^FS\n");
  }

  /**
   * Escapes reserved ZPL characters.
   * 
   * @param text The text to escape.
   * @return The escaped text.
   */
  private String escape(String text) {
    if (text == null)
      return "";
    return text.replace("^", "\\^");
  }

  /**
   * Sets the default font.
   * 
   * @param font     The font identifier.
   * @param height   The font height.
   * @param width    The font width.
   * @param rotation The font rotation.
   */
  public void setFont(String font, int height, int width, int rotation) {
    builder.append("^CF").append(font).append(",").append(height).append(",").append(width).append("\n");
  }

  /**
   * Draws a formatted text block.
   * 
   * @param x     The X coordinate of the text block.
   * @param y     The Y coordinate of the text block.
   * @param text  The text to draw.
   * @param width The width of the text block.
   * @param lines The maximum number of lines.
   * @param align The text alignment.
   */
  public void drawTextBlock(int x, int y, String text, int width, int lines, String align) {
    builder.append("^FO").append(x).append(",").append(y).append("\n").append("^FB").append(width).append(",")
        .append(lines).append(",0,").append(alignToZpl(align)).append(",0\n").append("^FD").append(escape(text))
        .append("^FS\n");
  }

  /**
   * Converts a text alignment value to a ZPL alignment code.
   * 
   * @param align The alignment value.
   * @return The ZPL alignment code.
   */
  private String alignToZpl(String align) {
    return switch (align.toLowerCase()) {
    case "center" -> "C";
    case "right" -> "R";
    case "justify" -> "J";
    default -> "L";
    };
  }

  /**
   * Draws a single two-dimensional line.
   * 
   * @param x1        The starting X coordinate.
   * @param y1        The starting Y coordinate.
   * @param x2        The ending X coordinate.
   * @param y2        The ending Y coordinate.
   * @param thickness The thickness of the line.
   */
  public void drawLine(int x1, int y1, int x2, int y2, int thickness) {

    final int dx = Math.abs(x2 - x1);
    final int dy = Math.abs(y2 - y1);

    final int width;
    final int height;

    if (dy == 0) {
      width = dx == 0 ? thickness : dx;
      height = thickness;
    } else if (dx == 0) {
      width = thickness;
      height = dy;
    } else {
      width = dx;
      height = dy;
    }

    builder.append("^FO").append(x1).append(",").append(y1).append("^GB").append(width).append(",").append(height)
        .append(",").append(thickness).append(",B,0^FS\n");
  }

  /**
   * Draws an image.
   * 
   * @param x    The X coordinate of the image.
   * @param y    The Y coordinate of the image.
   * @param name The name of the image.
   */
  public void drawImage(int x, int y, String name) {
    builder.append("^FO").append(x).append(",").append(y).append("^XG").append(name).append(",1,1^FS\n");
  }

  /**
   * Draws a reverse-color rectangle.
   * 
   * @param x      The X coordinate of the rectangle.
   * @param y      The Y coordinate of the rectangle.
   * @param width  The width of the rectangle.
   * @param height The height of the rectangle.
   */
  public void drawReverse(int x, int y, int width, int height) {
    builder.append("^FO").append(x).append(",").append(y).append("^GB").append(width).append(",").append(height)
        .append(",0,R,0^FS\n");
  }

  /**
   * Returns the generated ZPL content.
   * 
   * @return The assembled ZPL string.
   */
  @Override
  public String toString() {
    return builder.toString();
  }
}
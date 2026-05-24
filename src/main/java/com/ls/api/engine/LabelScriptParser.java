package com.ls.api.engine;

import com.ls.api.engine.ast.*;
import com.ls.api.engine.ast.node.impl.BarcodeCommandNode;
import com.ls.api.engine.ast.node.impl.BinaryExpression;
import com.ls.api.engine.ast.node.impl.BoxCommandNode;
import com.ls.api.engine.ast.node.impl.CommandNode;
import com.ls.api.engine.ast.node.impl.ConcatExpression;
import com.ls.api.engine.ast.node.impl.DocumentNode;
import com.ls.api.engine.ast.node.impl.ExpressionNode;
import com.ls.api.engine.ast.node.impl.IfStatementNode;
import com.ls.api.engine.ast.node.impl.ImageCommandNode;
import com.ls.api.engine.ast.node.impl.LineCommandNode;
import com.ls.api.engine.ast.node.impl.LiteralExpression;
import com.ls.api.engine.ast.node.impl.QrCodeCommandNode;
import com.ls.api.engine.ast.node.impl.ReverseCommandNode;
import com.ls.api.engine.ast.node.impl.StatementNode;
import com.ls.api.engine.ast.node.impl.TextBlockCommandNode;
import com.ls.api.engine.ast.node.impl.TextCommandNode;
import com.ls.api.engine.ast.node.impl.VariableExpression;

import java.util.ArrayList;
import java.util.List;

public class LabelScriptParser {

  /**
   * The default label width. Measured in pixels.
   */
  private static final int DEFAULT_WIDTH = 800;

  /**
   * The default label height. Measured in pixels.
   */
  private static final int DEFAULT_HEIGHT = 600;

  /**
   * The default dots per inch.
   */
  private static final int DEFAULT_DPI = 203;

  /**
   * All lines of the script being parsed.
   */
  private List<String> lines;

  /**
   * Current line cursor.
   */
  private int index;

  /**
   * @param script
   * @return
   * @throws LabelScriptEngineException
   */
  public DocumentNode parse(String script) throws LabelScriptEngineException {

    if (script == null || script.isBlank())
      return new DocumentNode(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DPI, List.of());

    script = script.replace("\\n", "\n");

    this.lines = List.of(script.split("\\R"));
    this.index = 0;

    int width = DEFAULT_WIDTH;
    int height = DEFAULT_HEIGHT;
    int dpi = DEFAULT_DPI;

    final List<StatementNode> statements = new ArrayList<>();

    while (hasMore()) {

      final String line = peek().trim();

      if (line.isEmpty() || line.startsWith(LabelScriptEngineConstants.COMMENT_CHAR)) {
        next();
        continue;
      }

      if (line.startsWith(LabelScriptEngineConstants.LABEL_SYNTAX + " ")) {

        final int[] size = parseLabelSize(line);
        width = size[0];
        height = size[1];
        dpi = size[2];

        next();
        continue;
      }

      if (line.startsWith(LabelScriptEngineConstants.CONDITIONAL_STATEMENT_SYNTAX + " ")) {
        statements.add(parseIf());
        continue;
      }

      statements.add(parseCommand(line));
      next();
    }

    return new DocumentNode(width, height, dpi, statements);
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private CommandNode parseCommand(String line) throws LabelScriptEngineException {

    if (line.startsWith(LabelScriptEngineConstants.COMMAND_TEXT_BLOCK + " "))
      return parseTextBlock(line);

    if (line.startsWith(LabelScriptEngineConstants.COMMAND_TEXT + " "))
      return parseText(line);

    if (line.startsWith(LabelScriptEngineConstants.COMMAND_BARCODE + " "))
      return parseBarcode(line);

    if (line.startsWith(LabelScriptEngineConstants.COMMAND_QR_CODE + " "))
      return parseQrCode(line);

    if (line.startsWith(LabelScriptEngineConstants.COMMAND_BOX + " "))
      return parseBox(line);

    if (line.startsWith(LabelScriptEngineConstants.COMMAND_LINE + " "))
      return parseLine(line);

    if (line.startsWith(LabelScriptEngineConstants.COMMAND_IMAGE + " "))
      return parseImage(line);

    if (line.startsWith(LabelScriptEngineConstants.COMMAND_REVERSE + " "))
      return parseReverse(line);

    throw new LabelScriptEngineException("Unknown command: " + line);
  }

  /**
   * @return
   * @throws LabelScriptEngineException
   */
  private IfStatementNode parseIf() throws LabelScriptEngineException {

    final String line = next().trim();
    final ExpressionNode condition = parseCondition(line.substring(3).trim());

    expect("{");

    List<StatementNode> thenBranch = parseBlock();
    List<StatementNode> elseBranch = List.of();

    if (hasMore() && peek().trim().startsWith("else")) {
      next();
      expect("{");
      elseBranch = parseBlock();
    }

    return new IfStatementNode(condition, thenBranch, elseBranch);
  }

  /**
   * @return
   * @throws LabelScriptEngineException
   */
  private List<StatementNode> parseBlock() throws LabelScriptEngineException {

    final List<StatementNode> statements = new ArrayList<>();

    while (hasMore()) {
      final String line = peek().trim();

      if (line.equals("}")) {
        next();
        break;
      }

      if (line.startsWith(LabelScriptEngineConstants.CONDITIONAL_STATEMENT_SYNTAX + " ")) {
        statements.add(parseIf());
        continue;
      }

      statements.add(parseCommand(line));
      next();
    }

    return statements;
  }

  /**
   * 
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private TextBlockCommandNode parseTextBlock(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final ExpressionNode expression = extractExpression(line);

    final int width = extractIntAfter(line, "width", 300);
    final int maxLines = extractIntAfter(line, "lines", 3);
    final String align = extractStringAfter(line, "align", "left");

    final FontSpec font = extractFont(line);

    return new TextBlockCommandNode(position, expression, width, maxLines, align, font);
  }

  /**
   *
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private FontSpec extractFont(String line) throws LabelScriptEngineException {

    final String fontName = extractStringAfter(line, "font", "A");

    final int fontHeight = extractIntAfter(line, "font-height", 30);
    final int fontWidth = extractIntAfter(line, "font-width", 20);
    final int rotation = extractIntAfter(line, "rotate", 0);

    return new FontSpec(fontName, fontHeight, fontWidth, rotation);
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private ReverseCommandNode parseReverse(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final Size size = extractSize(line);

    return new ReverseCommandNode(position, size);
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private ImageCommandNode parseImage(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final String name = extractQuotedString(line);

    return new ImageCommandNode(position, name);
  }

  /**
   * Parses a line command.
   *
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private LineCommandNode parseLine(String line) throws LabelScriptEngineException {

    final int fromIdx = line.indexOf("from");
    final int toIdx = line.indexOf("to");

    if (fromIdx == -1)
      throw new LabelScriptEngineException("Missing coordinates in '" + line + "'.");
  
    if (toIdx == -1)
      throw new LabelScriptEngineException("Missing coordinates in '" + line + "'.");

    final Position start = extractCoordinates(line, fromIdx);
    final Position end = extractCoordinates(line, toIdx);

    final int thickness = extractIntAfter(line, "thickness", 1);

    return new LineCommandNode(start, end, thickness);
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private TextCommandNode parseText(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final ExpressionNode expression = extractExpression(line);

    return new TextCommandNode(position, expression);
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private BarcodeCommandNode parseBarcode(String line) throws LabelScriptEngineException {

    final BarcodeType type = extractBarcodeType(line);
    final Position position = extractPosition(line);
    final ExpressionNode expression = extractExpression(line);

    final int height = extractIntAfter(line, "height", 100);
    final boolean human = line.contains("human");

    return new BarcodeCommandNode(type, position, expression, height, human);
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private QrCodeCommandNode parseQrCode(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final ExpressionNode expression = extractExpression(line);

    final int size = extractIntAfter(line, "size", 4);
    final int ecc = extractIntAfter(line, "ecc", 2);

    return new QrCodeCommandNode(position, expression, size, ecc);
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private BoxCommandNode parseBox(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final Size size = extractSize(line);
    final int border = extractIntAfter(line, "border", 1);

    return new BoxCommandNode(position, size, border);
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private int[] parseLabelSize(String line) throws LabelScriptEngineException {
    try {

      final String[] parts = line.split("\\s+");
      final String[] dims = parts[1].split("x");

      final int width = Integer.parseInt(dims[0]);
      final int height = Integer.parseInt(dims[1]);
      final int dpi = (parts.length >= 4 && parts[2].equalsIgnoreCase("dpi")) ? Integer.parseInt(parts[3]) : DEFAULT_DPI;

      return new int[] { width, height, dpi };
    } catch (Exception exception) {
      throw new LabelScriptEngineException("Invalid label directive in '" + line + "'.");
    }
  }

  /**
   * @param raw
   * @return
   * @throws LabelScriptEngineException
   */
  private ExpressionNode parseCondition(String raw) throws LabelScriptEngineException {

    raw = raw.trim();

    if (!raw.contains(" ")) {
      return parseExpression(raw);
    }

    final String[] parts = raw.split("\\s+");

    if (parts.length != 3) {
      throw new LabelScriptEngineException("Invalid condition '" + raw + "'.");
    }

    final ExpressionNode left = parseExpression(parts[0]);
    final ExpressionNode right = parseExpression(parts[2]);

    BinaryOperator operator = switch (parts[1]) {
    case "==" -> BinaryOperator.EQUALS;
    case "!=" -> BinaryOperator.NOT_EQUALS;
    case ">" -> BinaryOperator.GREATER_THAN;
    case ">=" -> BinaryOperator.GREATER_OR_EQUAL;
    case "<" -> BinaryOperator.LESS_THAN;
    case "<=" -> BinaryOperator.LESS_OR_EQUAL;
    default -> throw new LabelScriptEngineException("Unknown operator '" + parts[1] + "'.");
    };

    return new BinaryExpression(left, operator, right);
  }

  /**
   * @param token
   * @return
   */
  private ExpressionNode parseExpression(String token) {

    if (token.startsWith("${") && token.endsWith("}")) {
      return new VariableExpression(token.substring(2, token.length() - 1));
    }

    if (token.startsWith("\"") && token.endsWith("\"")) {
      return new LiteralExpression(token.substring(1, token.length() - 1));
    }

    return new LiteralExpression(token);
  }

  /**
   * 
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private ExpressionNode extractExpression(String line) throws LabelScriptEngineException {

    final String region = extractExpressionRegion(line);
    final String[] tokens = region.split("\\s*\\+\\s*");

    if (tokens.length == 1) {
      return parseSingleExpression(tokens[0].trim());
    }

    final List<ExpressionNode> parts = new ArrayList<>();
    
    for (String token : tokens) {
      parts.add(parseSingleExpression(token.trim()));
    }
    
    return new ConcatExpression(parts);
  }

  /**
   * 
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private String extractExpressionRegion(String line) throws LabelScriptEngineException {

    final int quoteStart = line.indexOf('"');
    final int varStart = line.indexOf("${");

    int expressionStart = -1;
    
    if (quoteStart != -1 && varStart != -1) {
      expressionStart = Math.min(quoteStart, varStart);
    } else if (quoteStart != -1) {
      expressionStart = quoteStart;
    } else if (varStart != -1) {
      expressionStart = varStart;
    }

    if (expressionStart == -1)
      throw new LabelScriptEngineException("Expected string or variable in '" + line + "'.");

    final String[] keywords = { 
        " width ", " lines ", " align ", " height ", " size ", " font ", " thickness ", " human", " ecc ", " border "
    };

    int expressionEnd = line.length();
    
    for (String keyword : keywords) {
      
      final int idx = line.indexOf(keyword, expressionStart);
      
      if (idx != -1 && idx < expressionEnd)
        expressionEnd = idx;
    }

    return line.substring(expressionStart, expressionEnd).trim();
  }

  /**
   * Parses a single expression token.
   */
  private ExpressionNode parseSingleExpression(String token) throws LabelScriptEngineException {

    if (token.startsWith("${") && token.endsWith("}")) {
      return new VariableExpression(token.substring(2, token.length() - 1));
    }

    if (token.startsWith("\"") && token.endsWith("\"")) {
      return new LiteralExpression(token.substring(1, token.length() - 1));
    }

    throw new LabelScriptEngineException(
        "Unrecognised expression token '" + token + "'. Expected \"literal\" or ${variable}.");
  }

  /**
   * Extracts a coordinate point from a line.
   *
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private Position extractPosition(String line) throws LabelScriptEngineException {

    final int at = line.indexOf("at");

    if (at == -1)
      throw new LabelScriptEngineException("Missing coordinates in '" + line + "'.");

    return extractCoordinates(line, at);
  }

  /**
   * 
   * @param line
   * @param searchFrom
   * @return
   * @throws LabelScriptEngineException
   */
  private Position extractCoordinates(String line, int searchFrom) throws LabelScriptEngineException {

    final int open = line.indexOf('(', searchFrom);
    final int close = line.indexOf(')', searchFrom);

    if (open == -1 || close == -1 || close < open)
      throw new LabelScriptEngineException("Missing coordinates in '" + line + "'.");

    final String[] parts = line.substring(open + 1, close).split(",");

    if (parts.length < 2)
      throw new LabelScriptEngineException("Expected two coordinates in '" + line + "'.");

    try {
      return new Position(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    } catch (NumberFormatException e) {
      throw new LabelScriptEngineException("Non-integer coordinate in '" + line + "'.");
    }
  }

  /**
   * 
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private Size extractSize(String line) throws LabelScriptEngineException {

    final int idx = line.indexOf("size");

    if (idx == -1)
      throw new LabelScriptEngineException("Missing size in '" + line + "'.");

    final int open = line.indexOf('(', idx);
    final int close = line.indexOf(')', idx);

    if (open == -1 || close == -1 || close < open)
      throw new LabelScriptEngineException("Missing width and height after size in '" + line + "'.");

    final String[] parts = line.substring(open + 1, close).split(",");

    if (parts.length < 2)
      throw new LabelScriptEngineException("Expected two dimensions in '" + line + "'.");

    try {
      return new Size(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    } catch (NumberFormatException exception) {
      throw new LabelScriptEngineException("Non-integer dimension in '" + line + "'.");
    }
  }

  /**
   * 
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private BarcodeType extractBarcodeType(String line) throws LabelScriptEngineException {

    if (line.contains("code128"))
      return BarcodeType.CODE128;

    if (line.contains("code39"))
      return BarcodeType.CODE39;

    if (line.contains("ean13"))
      return BarcodeType.EAN13;

    throw new LabelScriptEngineException("Unknown barcode type in '" + line + "'.");
  }

  /**
   * Extracts an integer value that follows the given keyword on the line.
   * 
   * @param line
   * @param key
   * @param defaultValue
   * @return
   * @throws LabelScriptEngineException
   */
  private int extractIntAfter(String line, String key, int defaultValue) throws LabelScriptEngineException {

    final int idx = line.indexOf(key);

    if (idx == -1)
      return defaultValue;

    final String after = line.substring(idx + key.length()).trim();
    final String[] parts = after.split("\\s+");

    if (parts.length == 0 || parts[0].isBlank())
      throw new LabelScriptEngineException("Missing integer value after '" + key + "' in '" + line + "'.");

    try {
      return Integer.parseInt(parts[0]);
    } catch (NumberFormatException e) {
      throw new LabelScriptEngineException(
          "Expected integer after '" + key + "' but got '" + parts[0] + "' in '" + line + "'.");
    }
  }

  /**
   * @param expected
   * @throws LabelScriptEngineException
   */
  private void expect(String expected) throws LabelScriptEngineException {

    final String line = next().trim();

    if (!line.equals(expected)) {
      throw new LabelScriptEngineException("Expected '" + expected + "' but returned '" + line + "'.");
    }
  }

  /**
   * @param line
   * @return
   * @throws LabelScriptEngineException
   */
  private String extractQuotedString(String line) throws LabelScriptEngineException {

    final int first = line.indexOf('"');
    final int last = line.lastIndexOf('"');

    if (first == -1 || last == -1 || first == last) {
      throw new LabelScriptEngineException("Expected quoted string in " + line + ".");
    }

    return line.substring(first + 1, last);
  }

  /**
   * @param line
   * @param key
   * @param defaultValue
   * @return
   */
  private String extractStringAfter(String line, String key, String defaultValue) {

    int idx = line.indexOf(key);

    if (idx == -1) {
      return defaultValue;
    }

    final String after = line.substring(idx + key.length()).trim();
    final String[] parts = after.split("\\s+");

    if (parts.length == 0) {
      return defaultValue;
    }

    return parts[0].trim();
  }

  /**
   * @return
   */
  private boolean hasMore() {
    return index < lines.size();
  }

  /**
   * @return
   */
  private String peek() {
    return lines.get(index);
  }

  /**
   * @return
   */
  private String next() {
    return lines.get(index++);
  }
}

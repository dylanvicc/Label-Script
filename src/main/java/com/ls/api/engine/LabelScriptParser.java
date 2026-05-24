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
   * Parses a label script into a document node. Extracts label metadata and all
   * executable statements.
   * 
   * @param script The raw label script.
   * @return The parsed document node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Parses a command statement from a line. Delegates parsing based on the
   * command keyword.
   * 
   * @param line The raw command line.
   * @return The parsed command node.
   * @throws LabelScriptEngineException Thrown when the command is unknown.
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
   * Parses an if statement and its nested branches. Supports optional else
   * blocks.
   * 
   * @return The parsed if statement node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Parses a nested statement block. Continues until the closing brace is
   * encountered.
   * 
   * @return The parsed statement list.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Parses a text block command. Extracts position, formatting, and text
   * expression data.
   * 
   * @param line The raw command line.
   * @return The parsed text block command node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Extracts font configuration values from a command line.
   * 
   * @param line The raw command line.
   * @return The extracted font specification.
   * @throws LabelScriptEngineException Thrown when parsing fails.
   */
  private FontSpec extractFont(String line) throws LabelScriptEngineException {

    final String fontName = extractStringAfter(line, "font", "A");

    final int fontHeight = extractIntAfter(line, "font-height", 30);
    final int fontWidth = extractIntAfter(line, "font-width", 20);
    final int rotation = extractIntAfter(line, "rotate", 0);

    return new FontSpec(fontName, fontHeight, fontWidth, rotation);
  }

  /**
   * Parses a reverse-fill command.
   * 
   * @param line The raw command line.
   * @return The parsed reverse command node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
   */
  private ReverseCommandNode parseReverse(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final Size size = extractSize(line);

    return new ReverseCommandNode(position, size);
  }

  /**
   * Parses an image command. Extracts the image name and render position.
   * 
   * @param line The raw command line.
   * @return The parsed image command node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
   */
  private ImageCommandNode parseImage(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final String name = extractQuotedString(line);

    return new ImageCommandNode(position, name);
  }

  /**
   * Parses a line drawing command. Extracts start and end coordinates and line
   * thickness.
   * 
   * @param line The raw command line.
   * @return The parsed line command node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Parses a text command. Extracts the text position and expression.
   * 
   * @param line The raw command line.
   * @return The parsed text command node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
   */
  private TextCommandNode parseText(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final ExpressionNode expression = extractExpression(line);

    return new TextCommandNode(position, expression);
  }

  /**
   * Parses a barcode command. Extracts barcode type, dimensions, and expression
   * data.
   * 
   * @param line The raw command line.
   * @return The parsed barcode command node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Parses a QR code command. Extracts QR position, size, and expression data.
   * 
   * @param line The raw command line.
   * @return The parsed QR code command node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
   */
  private QrCodeCommandNode parseQrCode(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final ExpressionNode expression = extractExpression(line);

    final int size = extractIntAfter(line, "size", 4);
    final int ecc = extractIntAfter(line, "ecc", 2);

    return new QrCodeCommandNode(position, expression, size, ecc);
  }

  /**
   * Parses a box command. Extracts dimensions and border settings.
   * 
   * @param line The raw command line.
   * @return The parsed box command node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
   */
  private BoxCommandNode parseBox(String line) throws LabelScriptEngineException {

    final Position position = extractPosition(line);
    final Size size = extractSize(line);
    final int border = extractIntAfter(line, "border", 1);

    return new BoxCommandNode(position, size, border);
  }

  /**
   * Parses the label size directive. Extracts width, height, and optional DPI
   * values.
   * 
   * @param line The raw directive line.
   * @return The parsed size configuration.
   * @throws LabelScriptEngineException Thrown when parsing fails.
   */
  private int[] parseLabelSize(String line) throws LabelScriptEngineException {
    try {

      final String[] parts = line.split("\\s+");
      final String[] dims = parts[1].split("x");

      final int width = Integer.parseInt(dims[0]);
      final int height = Integer.parseInt(dims[1]);
      final int dpi = (parts.length >= 4 && parts[2].equalsIgnoreCase("dpi")) ? Integer.parseInt(parts[3])
          : DEFAULT_DPI;

      return new int[] { width, height, dpi };
    } catch (Exception exception) {
      throw new LabelScriptEngineException("Invalid label directive in '" + line + "'.");
    }
  }

  /**
   * Parses a conditional expression. Supports binary comparison operators.
   * 
   * @param raw The raw condition string.
   * @return The parsed expression node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Parses a single expression token. Supports literals and variable references.
   * 
   * @param token The raw expression token.
   * @return The parsed expression node.
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
   * Extracts a full expression region from a command line. Supports concatenated
   * expressions.
   * 
   * @param line The raw command line.
   * @return The parsed expression node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Extracts the raw expression region from a command line. Stops parsing before
   * command modifiers and attributes.
   * 
   * @param line The raw command line.
   * @return The extracted expression region.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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

    final String[] keywords = { " width ", " lines ", " align ", " height ", " size ", " font ", " thickness ",
        " human", " ecc ", " border " };

    int expressionEnd = line.length();

    for (String keyword : keywords) {

      final int idx = line.indexOf(keyword, expressionStart);

      if (idx != -1 && idx < expressionEnd)
        expressionEnd = idx;
    }

    return line.substring(expressionStart, expressionEnd).trim();
  }

  /**
   * Parses a single expression token. Supports string literals and variable
   * references.
   * 
   * @param token The raw expression token.
   * @return The parsed expression node.
   * @throws LabelScriptEngineException Thrown when parsing fails.
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
   * Extracts a position from a command line. Looks for coordinates following the
   * 'at' keyword.
   * 
   * @param line The raw command line.
   * @return The extracted position.
   * @throws LabelScriptEngineException Thrown when coordinates are invalid.
   */
  private Position extractPosition(String line) throws LabelScriptEngineException {

    final int at = line.indexOf("at");

    if (at == -1)
      throw new LabelScriptEngineException("Missing coordinates in '" + line + "'.");

    return extractCoordinates(line, at);
  }

  /**
   * Extracts coordinate values from a command line. Parses coordinates in point
   * format.
   * 
   * @param line       The raw command line.
   * @param searchFrom The index to begin searching from.
   * @return The extracted position.
   * @throws LabelScriptEngineException Thrown when coordinates are invalid.
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
   * Extracts a size definition from a command line. Parses dimensions in width
   * and height format.
   * 
   * @param line The raw command line.
   * @return The extracted size.
   * @throws LabelScriptEngineException Thrown when dimensions are invalid.
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
   * Extracts the barcode type from a command line. Supports Code128, Code39, and
   * EAN13 formats.
   * 
   * @param line The raw command line.
   * @return The extracted barcode type.
   * @throws LabelScriptEngineException Thrown when the barcode type is unknown.
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
   * Extracts an integer value following a keyword. Returns the default value if
   * the keyword is missing.
   * 
   * @param line         The raw command line.
   * @param key          The keyword to search for.
   * @param defaultValue The fallback value.
   * @return The extracted integer value.
   * @throws LabelScriptEngineException Thrown when the value is invalid.
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
   * Validates the next line against an expected token.
   * 
   * @param expected The expected line value.
   * @throws LabelScriptEngineException Thrown when the value does not match.
   */
  private void expect(String expected) throws LabelScriptEngineException {

    final String line = next().trim();

    if (!line.equals(expected)) {
      throw new LabelScriptEngineException("Expected '" + expected + "' but returned '" + line + "'.");
    }
  }

  /**
   * Extracts a quoted string from a command line.
   * 
   * @param line The raw command line.
   * @return The extracted string value.
   * @throws LabelScriptEngineException Thrown when quotes are missing.
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
   * Extracts a string value following a keyword. Returns the default value if the
   * keyword is missing.
   * 
   * @param line         The raw command line.
   * @param key          The keyword to search for.
   * @param defaultValue The fallback value.
   * @return The extracted string value.
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
   * Returns whether more lines remain to be parsed.
   * 
   * @return True if more lines exist.
   */
  private boolean hasMore() {
    return index < lines.size();
  }

  /**
   * Returns the current line without advancing the cursor.
   * 
   * @return The current line.
   */
  private String peek() {
    return lines.get(index);
  }

  /**
   * Returns the current line and advances the cursor.
   * 
   * @return The next line.
   */
  private String next() {
    return lines.get(index++);
  }
}
package com.ls.api.engine.transpile.zpl;

import com.ls.api.engine.LabelScriptEngineException;
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
import com.ls.api.engine.transpile.Transpiler;
import com.ls.api.model.PrintType;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ZplTranspiler implements Transpiler {

  @Override
  public PrintType getType() {
    return PrintType.ZPL;
  }

  @Override
  public String transpile(DocumentNode document, Map<String, Object> data) throws LabelScriptEngineException {

    final ZplAssembler assembler = new ZplAssembler();
    assembler.beginLabel();

    for (StatementNode statement : document.getStatements()) {
      emitStatement(assembler, statement, data);
    }

    assembler.endLabel();
    return assembler.toString();
  }

  /**
   * @param assembler
   * @param statement
   * @param data
   * @throws LabelScriptEngineException
   */
  private void emitStatement(ZplAssembler assembler, StatementNode statement, Map<String, Object> data) throws LabelScriptEngineException {

    if (statement instanceof CommandNode command) {
      emitCommand(assembler, command, data);
      return;
    }

    if (statement instanceof IfStatementNode ifStatement) {
      boolean condition = evaluateBoolean(ifStatement.getCondition(), data);

      final List<StatementNode> branch = condition ? ifStatement.getThenBranch() : ifStatement.getElseBranch();

      for (StatementNode inner : branch) {
        emitStatement(assembler, inner, data);
      }

      return;
    }

    throw new LabelScriptEngineException("Unsupported statement encountered '" + statement.getClass().getSimpleName() + "'.");
  }

  /**
   * @param assembler
   * @param command
   * @param data
   * @throws LabelScriptEngineException
   */
  private void emitCommand(ZplAssembler assembler, CommandNode command, Map<String, Object> data) throws LabelScriptEngineException {

    if (command instanceof TextCommandNode text) {
     
      final String value = evaluateString(text.getText(), data);
      final Position position = text.getPosition();
      
      assembler.drawText(position.getX(), position.getY(), value);
      return;
    }

    if (command instanceof TextBlockCommandNode block) {
      
      final String value = evaluateString(block.getText(), data);
      final Position position = block.getPosition();
      final FontSpec font = block.getFont();
      
      assembler.setFont(font.getFontName(), font.getHeight(), font.getWidth(), font.getRotation());
      assembler.drawTextBlock(position.getX(), position.getY(), value, block.getWidth(), block.getMaxLines(), block.getAlignment());
      return;
    }

    if (command instanceof BoxCommandNode box) {
      
      final Position position = box.getPosition();
      final Size size = box.getSize();
      
      assembler.drawBox(position.getX(), position.getY(), size.getWidth(), size.getHeight(), box.getBorderThickness());
      return;
    }

    if (command instanceof LineCommandNode line) {
      assembler.drawLine(line.getStart().getX(), line.getStart().getY(), line.getEnd().getX(), line.getEnd().getY(), line.getThickness());
      return;
    }

    if (command instanceof ImageCommandNode image) {
      assembler.drawImage(image.getPosition().getX(), image.getPosition().getY(), image.getImageName());
      return;
    }

    if (command instanceof ReverseCommandNode reverse) {
      assembler.drawReverse(reverse.getPosition().getX(), reverse.getPosition().getY(), reverse.getSize().getWidth(), reverse.getSize().getHeight());
      return;
    }

    if (command instanceof BarcodeCommandNode barcode) {

      final String value = evaluateString(barcode.getValue(), data);
      final Position position = barcode.getPosition();

      switch (barcode.getType()) {
      case CODE128 -> assembler.drawBarcodeCode128(position.getX(), position.getY(), value, barcode.getHeight(), barcode.isHumanReadable());
      case CODE39  -> assembler.drawBarcodeCode39(position.getX(), position.getY(), value, barcode.getHeight(), barcode.isHumanReadable());
      case EAN13   -> assembler.drawBarcodeEan13(position.getX(), position.getY(), value, barcode.getHeight(), barcode.isHumanReadable());
      }
      return;
    }

    if (command instanceof QrCodeCommandNode qr) {
      
      final String value = evaluateString(qr.getValue(), data);
      final Position position = qr.getPosition();
      
      assembler.drawQrCode(position.getX(), position.getY(), value, qr.getModuleSize(), qr.getErrorCorrectionLevel());
      return;
    }

    throw new LabelScriptEngineException("Unknown command '" + command.getClass().getSimpleName() + "'.");
  }

  /**
   * @param expression
   * @param data
   * @return
   * @throws LabelScriptEngineException
   */
  private String evaluateString(ExpressionNode expression, Map<String, Object> data) throws LabelScriptEngineException {

    if (expression instanceof LiteralExpression lit)
      return lit.getValue();

    if (expression instanceof VariableExpression var) {
      final Object value = resolvePath(data, var.getPath());
      return value != null ? value.toString() : "";
    }

    if (expression instanceof ConcatExpression concat) {

      final StringBuilder builder = new StringBuilder();

      for (ExpressionNode part : concat.getParts()) {
        builder.append(evaluateString(part, data));
      }

      return builder.toString();
    }

    throw new LabelScriptEngineException("Unsupported string expression '" + expression.getClass().getSimpleName() + "'.");
  }

  /**
   * @param expression
   * @param data
   * @return
   * @throws LabelScriptEngineException
   */
  private boolean evaluateBoolean(ExpressionNode expression, Map<String, Object> data) throws LabelScriptEngineException {

    if (expression instanceof LiteralExpression lit) {
      return Boolean.parseBoolean(lit.getValue());
    }

    if (expression instanceof VariableExpression var) {
      final Object value = resolvePath(data, var.getPath());

      if (value instanceof Boolean bool)
        return bool;

      if (value == null)
        return false;

      return Boolean.parseBoolean(value.toString());
    }

    if (expression instanceof BinaryExpression bin) {

      final String left = evaluateString(bin.getLeft(), data);
      final String right = evaluateString(bin.getRight(), data);

      return switch (bin.getOperator()) {
      case EQUALS -> left.equals(right);
      case NOT_EQUALS -> !left.equals(right);
      case GREATER_THAN -> compare(left, right) > 0;
      case GREATER_OR_EQUAL -> compare(left, right) >= 0;
      case LESS_THAN -> compare(left, right) < 0;
      case LESS_OR_EQUAL -> compare(left, right) <= 0;
      };
    }

    throw new LabelScriptEngineException("Unsupported boolean expression '" + expression.getClass().getSimpleName() + "'.");
  }

  /**
   * Compares two strings as doubles, falling back to lexicographic order.
   *
   * @param first  the left-hand side of the comparison.
   * @param second the right-hand side of the comparison.
   * @return Negative, zero, or positive.
   */
  private int compare(String first, String second) {
    try {
      return Double.compare(Double.parseDouble(first), Double.parseDouble(second));
    } catch (NumberFormatException exception) {
      return first.compareTo(second);
    }
  }

  /**
   * @param data
   * @param path
   * @return
   */
  private Object resolvePath(Map<String, Object> data, String path) {
    Object current = data;
    for (String part : path.split("\\.")) {
      if (!(current instanceof Map<?, ?> map))
        return null;
      current = map.get(part);
      if (current == null)
        return null;
    }
    return current;
  }
}

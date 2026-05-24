package com.ls.api.engine.transpiler;

import java.util.Map;

import com.ls.api.engine.LabelScriptEngineException;
import com.ls.api.engine.ast.node.impl.DocumentNode;
import com.ls.api.model.PrinterType;

public interface Transpiler {

  /**
   * Returns the {@link PrinterType} for this implementation.
   * 
   * @return The returned type.
   */
  PrinterType getType();

  /**
   * 
   * @param document
   * @param data
   * @return
   * @throws LabelScriptEngineException
   */
  String transpile(DocumentNode document, Map<String, Object> data) throws LabelScriptEngineException;
}

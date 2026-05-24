package com.ls.api.engine.transpile;

import java.util.Map;

import com.ls.api.engine.LabelScriptEngineException;
import com.ls.api.engine.ast.node.impl.DocumentNode;
import com.ls.api.model.PrintType;

public interface Transpiler {

  /**
   * Returns the {@link PrintType} for this implementation.
   * 
   * @return The returned type.
   */
  PrintType getType();

  /**
   * 
   * @param document
   * @param data
   * @return The translated script content.
   * @throws LabelScriptEngineException The exception thrown if an error is
   *                                    encountered within the engine.
   */
  String transpile(DocumentNode document, Map<String, Object> data) throws LabelScriptEngineException;
}

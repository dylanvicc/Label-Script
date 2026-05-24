package com.ls.api.engine.transpile;

import java.util.Map;

import com.ls.api.engine.LabelScriptEngineException;
import com.ls.api.engine.ast.node.impl.DocumentNode;
import com.ls.api.model.PrintType;

public interface Transpiler {

  /**
   * Returns the {@link PrintType} supported by this translation.
   * 
   * @return The supported type.
   */
  PrintType getType();

  /**
   * Translates a parsed document into printer-specific output.
   * 
   * @param document The parsed document node.
   * @param data     The runtime variable data.
   * @return The translated script content.
   * @throws LabelScriptEngineException The exception thrown when translation fails.
   */
  String transpile(DocumentNode document, Map<String, Object> data) throws LabelScriptEngineException;
}
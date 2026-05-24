package com.ls.api.engine;

import com.ls.api.engine.ast.node.impl.DocumentNode;
import com.ls.api.engine.transpile.Transpiler;
import com.ls.api.model.PrintType;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LabelScriptEngine {

  private final List<Transpiler> transpilers;

  public LabelScriptEngine(List<Transpiler> transpilers) {
    this.transpilers = transpilers;
  }

  /**
   * Performs the translation of the label-script code to the appropriate printer
   * recognized code.
   * 
   * @param script The label-script code to be translated.
   * @param data   The external values to be utilized within this script.
   * @param type   The type of printer.
   * @return The script content that has been translated.
   * @throws LabelScriptEngineException The exception thrown if an error occurs
   *                                    within the engine.
   */
  public String execute(String script, Map<String, Object> data, PrintType type) throws LabelScriptEngineException {

    if (script == null || script.isBlank())
      throw new LabelScriptEngineException("Script cannot be null or empty.");

    final DocumentNode document = new LabelScriptParser().parse(script);

    final Transpiler transpiler = transpilers.stream().filter(t -> t.getType() == type).findFirst()
        .orElseThrow(() -> new LabelScriptEngineException("No transpiler registered for " + type + "."));

    return transpiler.transpile(document, data);
  }
}

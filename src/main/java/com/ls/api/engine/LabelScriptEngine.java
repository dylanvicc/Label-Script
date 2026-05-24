package com.ls.api.engine;

import com.ls.api.engine.ast.node.impl.DocumentNode;
import com.ls.api.engine.transpiler.Transpiler;
import com.ls.api.model.PrinterType;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LabelScriptEngine {

  /**
   *
   */
  private final List<Transpiler> transpilers;

  /**
   * Creates a new instance of this class file.
   * @param transpilers
   */
  public LabelScriptEngine(List<Transpiler> transpilers) {
    this.transpilers = transpilers;
  }

  /**
   * 
   * @param script
   * @param data
   * @param type
   * @return
   * @throws LabelScriptEngineException
   */
  public String execute(String script, Map<String, Object> data, PrinterType type) throws LabelScriptEngineException {

    if (script == null || script.isBlank())
      throw new LabelScriptEngineException("Script cannot be null or empty.");

    final DocumentNode document = new LabelScriptParser().parse(script);

    final Transpiler transpiler = transpilers.stream().filter(t -> t.getType() == type).findFirst()
        .orElseThrow(() -> new LabelScriptEngineException("No transpiler registered for " + type + "."));

    return transpiler.transpile(document, data);
  }
}

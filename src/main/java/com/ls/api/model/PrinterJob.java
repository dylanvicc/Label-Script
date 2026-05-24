package com.ls.api.model;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class PrinterJob implements Serializable {

  private final String id;
  private final String script;
  private final Map<String, Object> data;
  private final PrinterConfiguration configuration;

  public PrinterJob(String script, Map<String, Object> data, PrinterConfiguration configuration) {
    this.id = UUID.randomUUID().toString();
    this.script = script;
    this.data = data;
    this.configuration = configuration;
  }

  public String getId() {
    return id;
  }

  public String getScript() {
    return script;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public PrinterConfiguration getPrinterConfiguration() {
    return configuration;
  }
}
package com.ls.api.model;

import java.util.Map;

public class LabelRequest {

  private String script;
  private Map<String, Object> data;
  private PrinterConfiguration printer;

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }

  public PrinterConfiguration getPrinter() {
    return printer;
  }

  public void setPrinter(PrinterConfiguration printer) {
    this.printer = printer;
  }
}
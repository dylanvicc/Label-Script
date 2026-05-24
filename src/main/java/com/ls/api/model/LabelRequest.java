package com.ls.api.model;

import java.util.Map;

public class LabelRequest {

  private String script;
  private Map<String, Object> data;
  private PrintConfiguration printer;

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

  public PrintConfiguration getPrinter() {
    return printer;
  }

  public void setPrinter(PrintConfiguration printer) {
    this.printer = printer;
  }
}
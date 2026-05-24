package com.ls.api.model;

public class PrinterConfiguration {

  private PrinterType type;
  private PrinterTransport transport;
  private String target;

  public PrinterType getType() {
    return type;
  }

  public void setType(PrinterType type) {
    this.type = type;
  }

  public PrinterTransport getTransport() {
    return transport;
  }

  public void setTransport(PrinterTransport transport) {
    this.transport = transport;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }
}
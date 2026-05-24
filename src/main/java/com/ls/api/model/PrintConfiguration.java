package com.ls.api.model;

public class PrintConfiguration {

  private PrintType type;
  private PrintTransport transport;
  private String target;

  public PrintType getType() {
    return type;
  }

  public void setType(PrintType type) {
    this.type = type;
  }

  public PrintTransport getTransport() {
    return transport;
  }

  public void setTransport(PrintTransport transport) {
    this.transport = transport;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }
}
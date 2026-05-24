package com.ls.api.infs.printer.task;

import com.ls.api.model.PrintTransport;
import com.ls.api.model.PrintType;

public abstract class PrintTransportTask {

  private final PrintType type;
  private final PrintTransport transport;
  private final String target;
  private final String payload;

  /**
   * Attempts to send this payload to a printer via a defined protocol.
   * @return Denotes success or failure.
   */
  public abstract boolean send();

  /**
   * Aborts the transportation of this payload.
   * @return Denotes success or failure.
   */
  public abstract boolean abort();

  public PrintTransportTask(PrintType type, PrintTransport transport, String target, String payload) {
    this.type = type;
    this.transport = transport;
    this.target = target;
    this.payload = payload;
  }

  public PrintType getType() {
    return type;
  }

  public PrintTransport getTransport() {
    return transport;
  }

  public String getTarget() {
    return target;
  }

  public String getPayload() {
    return payload;
  }
}

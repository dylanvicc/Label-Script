package com.ls.api.infs.printer.task.impl;

import com.ls.api.infs.printer.task.PrintTransportTask;
import com.ls.api.model.PrintTransport;
import com.ls.api.model.PrintType;

public class CloudPrintTransportTask extends PrintTransportTask {

  public CloudPrintTransportTask(PrintType type, PrintTransport transport, String target, String payload) {
    super(type, transport, target, payload);
  }

  @Override
  public boolean send() {
    return false;
  }

  @Override
  public boolean abort() {
    return false;
  }
}
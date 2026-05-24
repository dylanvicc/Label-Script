package com.ls.api.infs.printer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ls.api.infs.printer.task.PrintTransportTask;
import com.ls.api.infs.printer.task.impl.CloudPrintTransportTask;
import com.ls.api.infs.printer.task.impl.NetworkPrintTransportTask;
import com.ls.api.infs.printer.task.impl.WindowsQueuePrintTransportTask;
import com.ls.api.infs.printer.task.impl.WiredPrintTransportTask;
import com.ls.api.model.PrintJob;
import com.ls.api.model.PrintTransport;
import com.ls.api.model.PrintType;

@Component
public class PrintDispatcher {

  private static final Logger logger = LoggerFactory.getLogger(PrintDispatcher.class);

  public boolean dispatch(PrintJob job, String payload) {

    if (job == null) 
      throw new IllegalArgumentException("Job cannot be null.");

    if (payload == null)
      throw new IllegalArgumentException("Payload cannot be null.");

    final PrintType type = job.getPrinterConfiguration().getType();
    final PrintTransport transport = job.getPrinterConfiguration().getTransport();
    final String target = job.getPrinterConfiguration().getTarget();

    logger.info("Dispatching print job {} to '{}' via {} ({}).", job.getId(), target, transport, type);

    try {
      switch (transport) {

      case NETWORK:
        return new NetworkPrintTransportTask(type, transport, target, payload).send();

      case WINDOWS_QUEUE:
        return new WindowsQueuePrintTransportTask(type, transport, target, payload).send();

      case CLOUD:
        return new CloudPrintTransportTask(type, transport, target, payload).send();

      case WIRED:
        return new WiredPrintTransportTask(type, transport, target, payload).send();

      }
    } catch (UnsupportedOperationException exception) {
      logger.error("Failed to dispatch print job {} to '{}' via {} ({}): {}", job.getId(), target, transport, type,
          exception.getMessage());
      throw exception;
    } catch (Exception exception) {
      logger.error("Failed to dispatch print job {} to '{}' via {} ({}).", job.getId(), target, transport, type,
          exception);
      throw exception;
    }

    return false;
  }
}

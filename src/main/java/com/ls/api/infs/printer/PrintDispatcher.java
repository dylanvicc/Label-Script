package com.ls.api.infs.printer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ls.api.model.PrinterJob;
import com.ls.api.model.PrinterTransport;
import com.ls.api.model.PrinterType;

@Component
public class PrintDispatcher {

  private static final Logger logger = LoggerFactory.getLogger(PrintDispatcher.class);

  /**
   * 
   * @param job
   * @param payload
   */
  public void dispatch(PrinterJob job, String payload) {

    if (job == null) {
      throw new IllegalArgumentException("Job cannot be null.");
    }

    if (payload == null) {
      throw new IllegalArgumentException("Payload cannot be null.");
    }

    final PrinterType type = job.getPrinterConfiguration().getType();
    final PrinterTransport transport = job.getPrinterConfiguration().getTransport();
    final String target = job.getPrinterConfiguration().getTarget();

    logger.info("Dispatching print job {} to '{}' via {} ({}).", job.getId(), target, transport, type);
    
    try {
      switch (transport) {

      case NETWORK:
        throw new UnsupportedOperationException("NETWORK transport is not yet implemented.");

      case USB:
        throw new UnsupportedOperationException("USB transport is not yet implemented.");

      case WINDOWS_QUEUE:
        throw new UnsupportedOperationException("WINDOWS_QUEUE transport is not yet implemented.");

      case CLOUD:
        throw new UnsupportedOperationException("CLOUD transport is not yet implemented.");

      case SERIAL:
        throw new UnsupportedOperationException("SERIAL transport is not yet implemented.");

      case LOCAL_ID:
        throw new UnsupportedOperationException("LOCAL_ID transport is not yet implemented.");
      }
    } catch (UnsupportedOperationException exception) {
      logger.error("Failed to dispatch print job {} to '{}' via {} ({}): {}", job.getId(), target, transport, type, exception.getMessage());
      throw exception;
    } catch (Exception exception) {
      logger.error("Failed to dispatch print job {} to '{}' via {} ({}).", job.getId(), target, transport, type, exception);
      throw exception;
    }
  }
}

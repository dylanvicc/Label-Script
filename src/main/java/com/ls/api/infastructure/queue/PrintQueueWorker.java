package com.ls.api.infastructure.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ls.api.engine.LabelScriptEngine;
import com.ls.api.infastructure.printer.PrintDispatcher;
import com.ls.api.model.PrinterJob;

@Component
public class PrintQueueWorker {

  private static final Logger logger = LoggerFactory.getLogger(PrintQueueWorker.class);

  private final PrintQueue queue;
  private final LabelScriptEngine engine;
  private final PrintDispatcher dispatcher;

  public PrintQueueWorker(PrintQueue queue, LabelScriptEngine engine, PrintDispatcher dispatcher) {
    this.queue = queue;
    this.engine = engine;
    this.dispatcher = dispatcher;
  }

  @Scheduled(fixedDelay = 200L)
  public void drain() {
    while (!queue.isEmpty()) {

      final PrinterJob job = queue.dequeue();

      if (job == null)
        break;

      try {
        dispatcher.dispatch(job, engine.execute(job.getScript(), job.getData(), job.getPrinterConfiguration().getType()));
      } catch (Exception exception) {
        logger.error("Error processing print job {}.", job.getId(), exception);
      }
    }
  }
}

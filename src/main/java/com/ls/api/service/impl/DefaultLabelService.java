package com.ls.api.service.impl;

import com.ls.api.model.LabelRequest;
import com.ls.api.model.LabelResponse;
import com.ls.api.model.PrinterConfiguration;
import com.ls.api.model.PrinterJob;
import com.ls.api.service.LabelService;
import com.ls.api.engine.LabelScriptEngine;
import com.ls.api.infs.printer.PrintDispatcher;

import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DefaultLabelService implements LabelService {

  private final LabelScriptEngine engine;
  private final PrintDispatcher dispatcher;

  public DefaultLabelService(LabelScriptEngine engine, PrintDispatcher dispatcher) {
    this.engine = engine;
    this.dispatcher = dispatcher;
  }

  @Override
  public LabelResponse render(LabelRequest request) {
    try {

      final String output = engine.execute(request.getScript(), request.getData(), request.getPrinter().getType());
      return new LabelResponse(output, Collections.emptyList(), Collections.emptyList());

    } catch (Exception exception) {
      return new LabelResponse(null, Collections.emptyList(), Collections.singletonList(exception.getMessage()));
    }
  }

  @Override
  public LabelResponse print(LabelRequest request) {
    try {

      final String output = engine.execute(request.getScript(), request.getData(), request.getPrinter().getType());
      final PrinterConfiguration configuration = request.getPrinter();
      final PrinterJob job = new PrinterJob(request.getScript(), request.getData(), configuration);

      dispatcher.dispatch(job, output);

      return new LabelResponse(output, Collections.emptyList(), Collections.emptyList());

    } catch (Exception exception) {
      return new LabelResponse(null, Collections.emptyList(), Collections.singletonList(exception.getMessage()));
    }
  }
}

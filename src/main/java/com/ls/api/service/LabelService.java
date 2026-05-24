package com.ls.api.service;

import com.ls.api.model.LabelRequest;
import com.ls.api.model.LabelResponse;

public interface LabelService {

  /**
   * Translates the label script provided into a printer specific language.
   * 
   * @param request The {@link LabelRequest} to translate.
   * @return The {@link LabelResponse} encapsulating the returned data.
   */
  LabelResponse render(LabelRequest request);

  /**
   * Translates the label script provided into a printer specific language and
   * dispatches a new print request. This request is queued and drained via the
   * {@link PrintQueue} and {@link PrintQueueWorker}.
   * 
   * @param request The {@link LabelRequest} to translate and queue for printing.
   * @return The {@link LabelResponse} encapsulating the returned data.
   */
  LabelResponse print(LabelRequest request);
}
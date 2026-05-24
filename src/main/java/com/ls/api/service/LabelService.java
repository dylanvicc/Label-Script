package com.ls.api.service;

import com.ls.api.model.LabelRequest;
import com.ls.api.model.LabelResponse;

public interface LabelService {

  /**
   * 
   * @param request
   * @return
   */
  LabelResponse render(LabelRequest request);

  /**
   * 
   * @param request
   * @return
   */
  LabelResponse print(LabelRequest request);
}
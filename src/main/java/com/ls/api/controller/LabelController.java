package com.ls.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ls.api.model.LabelRequest;
import com.ls.api.model.LabelResponse;
import com.ls.api.service.LabelService;

@RestController
@RequestMapping("/api/label")
public class LabelController {

  private final LabelService service;

  public LabelController(LabelService labelService) {
      this.service = labelService;
  }

  @PostMapping("/render")
  public LabelResponse render(@RequestBody LabelRequest request) {
      return service.render(request);
  }

  @PostMapping("/print")
  public LabelResponse print(@RequestBody LabelRequest request) {
      return service.print(request);
  }
}

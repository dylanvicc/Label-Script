package com.ls.api.model;

import java.util.List;

public class LabelResponse {

  private String output;
  private List<String> warnings;
  private List<String> errors;

  public LabelResponse(String output, List<String> warnings, List<String> errors) {
    this.output = output;
    this.warnings = warnings;
    this.errors = errors;
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }
}
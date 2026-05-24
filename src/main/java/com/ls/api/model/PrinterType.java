package com.ls.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PrinterType {
  ZPL("zpl"), 
  EPL("epl"), 
  ESCPOS("escpos");

  private final String name;

  PrinterType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @JsonCreator
  public static PrinterType from(String raw) {
      for (PrinterType type : values()) {
          if (type.name.equalsIgnoreCase(raw)) {
              return type;
          }
      }
      throw new IllegalArgumentException("Unknown printer type '" + raw + "'.");
  }
}
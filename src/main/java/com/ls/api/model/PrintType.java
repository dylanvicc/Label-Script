package com.ls.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PrintType {
  ZPL("zpl"), 
  EPL("epl"), 
  ESCPOS("escpos");

  private final String name;

  PrintType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @JsonCreator
  public static PrintType from(String raw) {
    for (PrintType type : values()) {
      if (type.name.equalsIgnoreCase(raw)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown printer type '" + raw + "'.");
  }
}
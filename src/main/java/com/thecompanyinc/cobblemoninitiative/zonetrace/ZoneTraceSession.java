package com.thecompanyinc.cobblemoninitiative.zonetrace;

import java.util.ArrayList;
import java.util.List;

/** In-progress zone trace session for a single player. */
public class ZoneTraceSession {

  public final String name;
  public String subtitle    = "";
  public String type        = "TOWN";
  public String dimension;
  public boolean announce   = true;
  public String color       = "#AAAAAA";
  public boolean hostileOnly = true;
  public boolean cylindrical = true;
  public int centerY         = 64;

  /** Recorded polygon vertices: each entry is {x, z}. */
  public final List<int[]> vertices = new ArrayList<>();

  public ZoneTraceSession(String name, String dimension) {
    this.name      = name;
    this.dimension = dimension;
  }
}

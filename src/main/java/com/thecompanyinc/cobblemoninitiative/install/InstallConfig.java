package com.thecompanyinc.cobblemoninitiative.install;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserialized form of {@code data/cobblemon_initiative/install.json}.
 *
 * <p>Mod dependencies belong in {@code fabric.mod.json} (the launcher resolves them).
 * NPC preset application is handled by {@code /cobblemon-initiative npc-map apply}
 * or by running {@code /function cobblemon_initiative:update_npc_presets} after
 * regenerating the mcfunction with {@code generate_npc_function}.
 */
public class InstallConfig {

  /**
   * Game rules to enforce on {@code /cobblemon-initiative install run}.
   * Keys are exact gamerule names (e.g. {@code "doMobSpawning"}).
   * Values are strings passed verbatim to {@code /gamerule <key> <value>}.
   * Special key {@code "_difficulty"} sets server difficulty instead.
   */
  public Map<String, String> gamerules = new LinkedHashMap<>();

  /**
   * When true, {@code /cobblemon-initiative install run} promotes the world to hardcore
   * (permadeath + difficulty locked to hard) via a runtime flip of the world's
   * {@code hardcore} flag. The client applies the hardcore UI on the next rejoin. Hardcore
   * already forces hard difficulty, so the {@code "_difficulty"} gamerule is a fallback for
   * worlds that aren't run in hardcore.
   */
  public boolean hardcore = false;

  /**
   * Named zones applied as SafeZones and written to the Map Frontiers frontier file
   * when running {@code /cobblemon-initiative install run}.
   */
  public List<InstallZone> zones = new ArrayList<>();

  /**
   * Mods the {@code install verify} report checks for. Presence is resolved via
   * FabricLoader; an entry is satisfied if its {@code modId} OR any {@code alias}
   * is loaded. {@code required} reflects the intended UPM 2 experience, not what
   * the loader enforces (only Cobblemon + Fabric are hard {@code depends}).
   */
  public List<ExpectedMod> expectedMods = new ArrayList<>();

  /** Datapack id fragments {@code install verify} checks for (matched case-insensitively). */
  public List<String> expectedDatapacks = new ArrayList<>();

  /** Resource packs surfaced as recommendations (name only — client packs aren't auto-detected). */
  public List<String> recommendedResourcePacks = new ArrayList<>();

  /** Shader packs surfaced as recommendations (name only — the active pack isn't auto-detected). */
  public List<String> recommendedShaders = new ArrayList<>();

  /** One expected mod for the verify report. */
  public static class ExpectedMod {
    public String modId;
    public String name;
    public boolean required = false;
    public List<String> aliases = new ArrayList<>();
    public String note = "";
  }
}

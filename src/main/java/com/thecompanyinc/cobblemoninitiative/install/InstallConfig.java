package com.thecompanyinc.cobblemoninitiative.install;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deserialized form of {@code data/cobblemon_initiative/install.json}.
 *
 * <p>Only gamerules live here. Mod dependencies belong in {@code fabric.mod.json}
 * (the launcher resolves them). NPC preset application is handled by
 * {@code /cobblemon-initiative npc-map apply} (backed by {@code npc_preset_map.json})
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
}

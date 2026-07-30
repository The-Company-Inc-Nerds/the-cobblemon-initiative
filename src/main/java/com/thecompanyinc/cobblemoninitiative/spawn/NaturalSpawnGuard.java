package com.thecompanyinc.cobblemoninitiative.spawn;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.SpecialSpawnConfig;
import kotlin.Unit;

/**
 * Keeps the curated "special" Pokémon (legendaries, mythicals, noble-exclusive lines — see
 * {@link SpecialSpawnConfig}) out of natural wild spawns, so they remain obtainable ONLY through
 * their scripted encounter.
 *
 * <p>Mechanism (Cobblemon 1.7.3, bytecode-verified): {@code CobblemonEvents.ENTITY_SPAWN} is a
 * cancelable event emitted ONLY by {@code SingleEntitySpawnAction} — the world spawner's spawn
 * step. Command spawns ({@code /spawnpokemon}, used by the shrine crystals and the Wisp-Lantern)
 * and direct {@code level.addFreshEntity()} spawns (the noble catch bodies) never emit it, so
 * cancelling here filters the natural spawner and NOTHING else. We subscribe to the base
 * {@code ENTITY_SPAWN} (rather than the {@code POKEMON_ENTITY_SPAWN} transform) and instanceof-check
 * the entity ourselves — the spawner only ever produces Pokémon anyway.
 */
public final class NaturalSpawnGuard {

  private NaturalSpawnGuard() {}

  public static void register() {
    CobblemonEvents.ENTITY_SPAWN.subscribe(Priority.NORMAL, event -> {
      if (!(event.getEntity() instanceof PokemonEntity mon)) return Unit.INSTANCE;
      String species = mon.getPokemon().getSpecies().getResourceIdentifier().getPath();

      // 1. Blacklist: cancel curated species so they stay scripted-only.
      SpecialSpawnConfig scfg = SpecialSpawnConfig.get();
      if (scfg.isPreventNaturalSpawns() && scfg.isBlacklisted(species)) {
        event.cancel();
        if (InitiativeInit.LOGGER.isDebugEnabled()) {
          InitiativeInit.LOGGER.debug("[SpecialSpawn] blocked natural spawn of {}", species);
        }
        return Unit.INSTANCE;
      }

      // 2. Wild-level scaling: re-level the survivor to the player's progression.
      applyWildLevel(mon);
      return Unit.INSTANCE;
    });
    InitiativeInit.LOGGER.info(
      "[SpecialSpawn] Natural-spawn guard armed ({} species blacklisted, enforcement={}).",
      SpecialSpawnConfig.get().getBlacklistedSpecies().size(),
      SpecialSpawnConfig.get().isPreventNaturalSpawns()
    );
  }

  /**
   * CLAMP a natural wild spawn's level into {@code [minLevel, cap + maxOffset]} using the nearest
   * player's badge-gated level cap — the natural (biome) level is preserved when it already sits in
   * range, and only outliers are pulled in (over-cap spawns knocked down to the ceiling, sub-floor
   * spawns raised to {@code minLevel}). {@code minLevel} is an absolute floor (0 = none). No-op if
   * disabled, no player is near, or the cap is unavailable.
   */
  private static void applyWildLevel(PokemonEntity mon) {
    com.thecompanyinc.cobblemoninitiative.config.WildLevelConfig cfg =
      com.thecompanyinc.cobblemoninitiative.config.WildLevelConfig.get();
    if (!cfg.isEnabled()) return;
    if (!(mon.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
    if (!(sl.getNearestPlayer(mon, 256.0) instanceof net.minecraft.server.level.ServerPlayer near)) return;
    com.thecompanyinc.cobblemoninitiative.levelcap.LevelCapManager lcm = InitiativeInit.getLevelCapManager();
    if (lcm == null) return;
    int cap = lcm.getLevelCap(near);
    int ceiling = Math.max(1, Math.min(100, cap + cfg.getMaxOffset()));
    int floor = Math.max(1, Math.min(ceiling, cfg.getMinLevel())); // floor >= 1 and never above the ceiling
    int natural;
    try {
      natural = mon.getPokemon().getLevel();
    } catch (Throwable ignored) {
      return;
    }
    int clamped = Math.max(floor, Math.min(ceiling, natural));
    if (clamped == natural) return; // already in range — keep the natural level
    try {
      mon.getPokemon().setLevel(clamped);
    } catch (Throwable ignored) {
      // never break a spawn over a re-level
    }
  }
}

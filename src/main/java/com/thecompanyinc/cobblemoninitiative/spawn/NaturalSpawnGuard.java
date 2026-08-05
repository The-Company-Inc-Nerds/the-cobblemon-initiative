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

      // 2. Safari-exclusive roster: any lure-table species is Preserve-only stock —
      //    cancelled WORLDWIDE, not just inside the fence, so booking a round is
      //    their only wild source. Bait lures spawn via addFreshEntity and bypass
      //    this event by construction (see class javadoc). Null-safe: no manager
      //    (init-order drift) skips the check; togglable via the safari config.
      if (isSafariExclusive(species)) {
        event.cancel();
        if (InitiativeInit.LOGGER.isDebugEnabled()) {
          InitiativeInit.LOGGER.debug("[SpecialSpawn] blocked natural spawn of safari-exclusive {}", species);
        }
        return Unit.INSTANCE;
      }

      // 3. Ridgewatch Preserve: no natural spawns inside the "Safari Zone" polygon —
      //    every round catch must come off a bait table (the exclusivity rule above
      //    covers table species; this keeps the OFF-table locals out of rounds too).
      //    Name-keyed SafeZone lookup, null-safe: a bare-mod world without the zone
      //    skips the check. Always-on, not round-gated.
      if (isInsideSafariZone(mon)) {
        event.cancel();
        return Unit.INSTANCE;
      }

      // 4. Wild-level scaling: re-level the survivor to the player's progression.
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
   * True when safari exclusivity is on and the species (resource-id path, lowercase) rides a
   * Preserve lure table. Reads live manager state so a ModMenu save/reload applies immediately.
   */
  private static boolean isSafariExclusive(String species) {
    com.thecompanyinc.cobblemoninitiative.safari.SafariManager safari =
      InitiativeInit.getSafariManager();
    if (safari == null) return false;
    return safari.getConfig().exclusiveSpecies && safari.getLureSpeciesIds().contains(species);
  }

  /** True when the entity stands inside the "Safari Zone" SafeZone (polygon-exact). */
  private static boolean isInsideSafariZone(PokemonEntity mon) {
    com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig cfg =
      com.thecompanyinc.cobblemoninitiative.NuzlockeInit.getConfig();
    if (cfg == null || cfg.getSafeZones() == null) return false;
    String dim = mon.level().dimension().location().toString();
    for (com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig.SafeZone zone : cfg.getSafeZones()) {
      if ("Safari Zone".equals(zone.name)) {
        return zone.contains(dim, mon.getBlockX(), mon.getBlockY(), mon.getBlockZ());
      }
    }
    return false;
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

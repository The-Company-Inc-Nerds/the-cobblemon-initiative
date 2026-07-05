package com.thecompanyinc.cobblemoninitiative.dex;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokedex.PokedexEntryProgress;
import com.cobblemon.mod.common.api.pokedex.PokedexManager;
import com.cobblemon.mod.common.api.pokedex.SpeciesDexRecord;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mirrors each player's Pokédex progress into the {@code dex_caught} scoreboard
 * objective (unique species with knowledge == CAUGHT), so datapack/dialog content can
 * gate on it — dialog gates use {@code "dex": {"op": "gte", "value": N}}, which
 * content_compile lowers to band tags ({@code dex_gte_15} etc.) maintained per tick
 * from this score. First consumer: the starter unlock ladder (15 → second partner,
 * 30 → the last one).
 *
 * <p>Polled every {@value #UPDATE_INTERVAL_TICKS} ticks rather than event-hooked:
 * dex knowledge changes through many paths (capture, trade, evolution, scanning),
 * and a 2-second lag on a gate threshold is imperceptible.
 */
public final class DexScoreManager {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  public static final String OBJECTIVE = "dex_caught";
  private static final int UPDATE_INTERVAL_TICKS = 40;

  private static int tickCounter;

  private DexScoreManager() {}

  public static void init() {
    ServerTickEvents.END_SERVER_TICK.register(DexScoreManager::tick);
  }

  private static void tick(MinecraftServer server) {
    if (++tickCounter < UPDATE_INTERVAL_TICKS) return;
    tickCounter = 0;
    if (server.getPlayerList().getPlayers().isEmpty()) return;

    Objective objective = server.getScoreboard().getObjective(OBJECTIVE);
    if (objective == null) {
      objective = server.getScoreboard().addObjective(
        OBJECTIVE,
        ObjectiveCriteria.DUMMY,
        Component.literal("Pokédex (caught)"),
        ObjectiveCriteria.RenderType.INTEGER,
        true,
        null
      );
    }

    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      try {
        PokedexManager dex = Cobblemon.playerDataManager.getPokedexData(player);
        int caught = 0;
        for (SpeciesDexRecord record : dex.getSpeciesRecords().values()) {
          if (record.getKnowledge() == PokedexEntryProgress.CAUGHT) {
            caught++;
          }
        }
        server.getScoreboard().getOrCreatePlayerScore(player, objective).set(caught);
      } catch (Exception e) {
        LOGGER.warn("Could not mirror dex progress for {}", player.getName().getString(), e);
      }
    }
  }
}

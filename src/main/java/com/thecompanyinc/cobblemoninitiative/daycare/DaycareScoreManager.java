package com.thecompanyinc.cobblemoninitiative.daycare;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mirrors each player's daycare custody count into the {@code ci_daycare} scoreboard
 * objective, so Bianca's dialog can be state-aware (0.7.0-alpha.3 playtest ruling:
 * fewer, smarter options) — content_compile lowers her entry gates to band tags
 * ({@code ci_daycare_gte_1} / {@code ci_daycare_gte_2}) maintained per tick from this
 * score, and the dialog ladder shows only the relevant deposit/withdraw buttons.
 *
 * <p>Polled every {@value #UPDATE_INTERVAL_TICKS} ticks (the {@link
 * com.thecompanyinc.cobblemoninitiative.dex.DexScoreManager} pattern) rather than
 * write-through: custody changes via deposit, withdraw, and load, and a 2-second lag
 * between a withdraw and the dialog ladder noticing is imperceptible across the
 * close-and-reopen the withdraw buttons force anyway.
 */
public final class DaycareScoreManager {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  public static final String OBJECTIVE = "ci_daycare";
  private static final int UPDATE_INTERVAL_TICKS = 40;

  private static int tickCounter;

  private DaycareScoreManager() {}

  public static void init() {
    ServerTickEvents.END_SERVER_TICK.register(DaycareScoreManager::tick);
  }

  private static void tick(MinecraftServer server) {
    if (++tickCounter < UPDATE_INTERVAL_TICKS) return;
    tickCounter = 0;
    if (server.getPlayerList().getPlayers().isEmpty()) return;
    DaycareManager daycare = InitiativeInit.getDaycareManager();
    if (daycare == null) return;

    Objective objective = server.getScoreboard().getObjective(OBJECTIVE);
    if (objective == null) {
      objective = server.getScoreboard().addObjective(
        OBJECTIVE,
        ObjectiveCriteria.DUMMY,
        Component.literal("Daycare (boarded)"),
        ObjectiveCriteria.RenderType.INTEGER,
        true,
        null
      );
    }

    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      try {
        int boarded = daycare.boardedCount(player.getUUID());
        server.getScoreboard().getOrCreatePlayerScore(player, objective).set(boarded);
      } catch (Exception e) {
        LOGGER.warn("Could not mirror daycare custody for {}", player.getName().getString(), e);
      }
    }
  }
}

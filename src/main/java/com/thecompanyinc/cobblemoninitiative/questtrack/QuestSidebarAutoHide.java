package com.thecompanyinc.cobblemoninitiative.questtrack;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.HudConfig;
import com.thecompanyinc.cobblemoninitiative.screen.QuestLogScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;

/**
 * Auto-hide timer for the ci_quest scoreboard sidebar (client-only; the render cancel
 * itself lives in {@code GuiSidebarMixin}). The sidebar pops for
 * {@link HudConfig#sidebarShowSeconds} whenever there is fresh context:
 *
 * <ul>
 *   <li><b>Any sidebar-visible change</b> — holders, scores, or per-score display
 *       components, hashed per tick off the vanilla-synced client scoreboard
 *       (single-player scale: a handful of lines, the hash is cheap). This is what
 *       pops it when a quest line updates.</li>
 *   <li><b>The ] / [ track keybinds</b> — poked directly from QuestTrackClient, so a
 *       track cycle that lands on the same sidebar text (single tracked quest) still
 *       shows, and without waiting on the server round-trip for the ▶ to move.</li>
 *   <li><b>The Quest Log screen closing</b> — fresh context after a review. Detected
 *       here by screen transition rather than a hook in the screen, so ESC, Shift+T
 *       toggle-close and replacement all count. No special case for the log being
 *       OPEN: the screen re-renders every quest itself and covers the HUD anyway.</li>
 * </ul>
 */
public final class QuestSidebarAutoHide {

  private static final String QUEST_OBJECTIVE = "ci_quest";

  /** Ticks left in the current show window; the sidebar renders while &gt; 0. */
  private static int showTicks;

  /** Hash of the last-seen sidebar-visible state; 0 = objective absent / no level. */
  private static int lastHash;

  private static boolean questLogWasOpen;

  private QuestSidebarAutoHide() {}

  public static void init() {
    ClientTickEvents.END_CLIENT_TICK.register(QuestSidebarAutoHide::tick);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
      showTicks = 0;
      lastHash = 0;
      questLogWasOpen = false;
    });

    // Breadcrumb: AutoHUD's scoreboard module (autohud.json5 → scoreboard.active) also
    // show-on-change-hides the sidebar; with both active the sidebar only appears when
    // the two timers agree. The harvested pack config should leave scoreboard alone —
    // this warn is the trail back here if it ever doesn't.
    if (
      HudConfig.get().autoHideQuestSidebar
        && FabricLoader.getInstance().isModLoaded("autohud")
    ) {
      InitiativeInit.LOGGER.warn(
        "AutoHUD is loaded while the quest-sidebar auto-hide is enabled — if AutoHUD's "
          + "scoreboard module is active the two will fight; disable scoreboard in "
          + "autohud.json5 (or the Stream HUD toggle in ModMenu).");
    }
  }

  /** Open (or refresh) the show window. */
  public static void show() {
    showTicks = HudConfig.get().sidebarShowSeconds * 20;
  }

  /**
   * The GuiSidebarMixin gate. Only ever true for the ci_quest objective — any other
   * sidebar (dev scoreboards, another mod's) is never touched.
   */
  public static boolean shouldHide(Objective objective) {
    return QUEST_OBJECTIVE.equals(objective.getName())
      && HudConfig.get().autoHideQuestSidebar
      && showTicks <= 0;
  }

  private static void tick(Minecraft client) {
    boolean questLogOpen = client.screen instanceof QuestLogScreen;
    if (questLogWasOpen && !questLogOpen) show();
    questLogWasOpen = questLogOpen;

    int hash = stateHash(client);
    if (hash != lastHash) {
      lastHash = hash;
      // Objective vanishing (hash → 0) is not fresh context — don't pop an empty board.
      if (hash != 0) show();
    } else if (showTicks > 0) {
      showTicks--;
    }
  }

  /**
   * Hash of exactly what the sidebar draws: the objective's display name plus every
   * non-scratch holder's name, score and synced display component (QuestLogScreen's
   * read: {@code isHidden()} is the {@code #}-prefix scratch check, {@code ownerName()}
   * the display-component-else-raw-holder string).
   */
  private static int stateHash(Minecraft client) {
    if (client.level == null) return 0;
    Scoreboard scoreboard = client.level.getScoreboard();
    Objective objective = scoreboard.getObjective(QUEST_OBJECTIVE);
    if (objective == null) return 0;
    int h = objective.getDisplayName().getString().hashCode();
    for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
      if (entry.isHidden()) continue;
      h = h * 31 + entry.owner().hashCode();
      h = h * 31 + entry.value();
      h = h * 31 + entry.ownerName().getString().hashCode();
    }
    return h;
  }
}

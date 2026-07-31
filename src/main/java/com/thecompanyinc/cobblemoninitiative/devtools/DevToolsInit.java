package com.thecompanyinc.cobblemoninitiative.devtools;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ONE dev-tooling entrypoint. Every dev command now hangs off a single top-level
 * {@code /ca-dev} root (2026-07-31) — the shipping {@code cobblemon-initiative}/{@code /ca} tree
 * carries no dev subtrees. Strip at 1.0.0 (TODO §2): remove this entrypoint from fabric.mod.json,
 * delete the devtools package, delete {@code command/TestCommands.java} + its register() call here,
 * and delete the devtest resources — the shipping command tree is untouched.
 *
 * <p>Consolidation (2026-07-11) re-homed the surviving tools here with their command
 * surface UNCHANGED (in-flight walks and wiki docs stay valid):
 * <ul>
 *   <li><b>GymMark wand</b> — {@code /ca-dev gym-mark …} (from fieldmark/,
 *       whose field-mark half was deleted: farm polygons are canonical in install.json)</li>
 *   <li><b>DevNote suite</b> — {@code /ca-dev npcnote} stick, {@code /ca-dev pos} capture,
 *       {@code /ca-dev debug} (the {@code smoke} checklist was retired 2026-07-31)</li>
 *   <li><b>dev subtree</b> — {@code /ca-dev badges|grant|heal|glow|phone|stage|place|note|marker|log}
 *       (from the shipping commands file) — flattened onto /ca-dev 2026-07-31</li>
 *   <li><b>bot harness</b> (2026-07-12) — {@code /ca-dev bot use|useitem|aim|interact}
 *       synthetic interactions + {@code /ca-dev bot autobattle} auto-battler, so Carpet
 *       fake players can right-click, throw Pokéballs, and WIN/LOSE battles headlessly
 *       (see {@link DevBotCommand} / {@link AutoBattler})</li>
 *   <li><b>test harness</b> — {@code /ca-dev test reload|data|registry|placement|all} headless
 *       diagnostics ({@link com.thecompanyinc.cobblemoninitiative.command.TestCommands})</li>
 * </ul>
 * zonetrace/ and the dev/npc_tour datapack functions were deleted outright (superseded by
 * the browser zone-mapper and {@code dev place} respectively).
 */
public class DevToolsInit implements ModInitializer {

  public static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-devtools");

  private static GymMarkStorage gymMarks;

  @Override
  public void onInitialize() {
    gymMarks = new GymMarkStorage();

    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
      // Every dev-only command lives under the ONE top-level /ca-dev root so the whole surface
      // strips in one place at 1.0.0. Each file registers its own /ca-dev literal; Brigadier merges.
      // /ca-dev gym-mark — the gym-gimmick coordinate pass (33 slots).
      GymMarkCommand.register(dispatcher, gymMarks);
      // /ca-dev … — badges/grant/heal/glow/phone/stage/place/note/marker/log.
      DevCommands.register(dispatcher);
      // /ca-dev bot … — synthetic interactions + autobattle toggle
      // for Carpet fake players (acts on the SOURCE player; drive via execute as).
      DevBotCommand.register(dispatcher);
      // /ca-dev test … — the headless diagnostics harness (moved off the shipping tree 2026-07-31).
      com.thecompanyinc.cobblemoninitiative.command.TestCommands.register(dispatcher);
    });
    // Auto-battler tick hook: submits first-legal battle choices for enrolled players.
    AutoBattler.register();
    // The double-click marking wand (gym-mark wand): right-click block/air handlers.
    GymMarkWand.registerEvents(gymMarks);
    // THE PRODUCER'S TOOL — the one-item walk over placement plan + gym slots
    // (fly/invuln while held, set/confirm clicks, Q = skip, chat notes, glint state).
    DevWandTool.registerEvents(gymMarks);

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      gymMarks.load(server);
      LOGGER.info("Dev tools loaded ({} gym mark(s)).", gymMarks.size());
    });
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> gymMarks.save());

    // npcnote stick + pos capture (registers its own callbacks).
    DevNoteInit.register();

    // /ca-dev marker — block-marker tool (left=anchor, right=confirm/box) + in-world highlight.
    DevMarkerManager.register();

    LOGGER.info("Dev tools initialized under /ca-dev (gym-mark, npcnote/pos/debug, marker, test, dev subtree).");
  }

  public static GymMarkStorage getGymMarks() {
    return gymMarks;
  }
}

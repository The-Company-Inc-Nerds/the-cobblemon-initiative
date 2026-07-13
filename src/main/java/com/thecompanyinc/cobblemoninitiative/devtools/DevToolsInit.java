package com.thecompanyinc.cobblemoninitiative.devtools;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ONE dev-tooling entrypoint. Strip at 1.0.0 (TODO §2): remove this entrypoint from
 * fabric.mod.json, delete the devtools package + the two devtest resources — done.
 *
 * <p>Consolidation (2026-07-11) re-homed the surviving tools here with their command
 * surface UNCHANGED (in-flight walks and wiki docs stay valid):
 * <ul>
 *   <li><b>GymMark wand</b> — {@code /cobblemon-initiative gym-mark …} (from fieldmark/,
 *       whose field-mark half was deleted: farm polygons are canonical in install.json)</li>
 *   <li><b>DevNote suite</b> — {@code npcnote} stick, {@code pos} capture, {@code smoke}
 *       checklist (formerly the DevNoteInit entrypoint)</li>
 *   <li><b>dev subtree</b> — {@code dev goto|badges|grant|kit} (from the shipping commands
 *       file) + {@code dev team|stage} test harness + {@code dev place} guided walk</li>
 *   <li><b>bot harness</b> (2026-07-12) — {@code dev bot use|useitem|aim|interact}
 *       synthetic interactions + {@code dev bot autobattle} auto-battler, so Carpet
 *       fake players can right-click, throw Pokéballs, and WIN/LOSE battles headlessly
 *       (see {@link DevBotCommand} / {@link AutoBattler})</li>
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
      // /cobblemon-initiative gym-mark — the gym-gimmick coordinate pass (33 slots).
      GymMarkCommand.register(dispatcher, gymMarks);
      // /cobblemon-initiative dev … — goto/badges/grant/kit + team/stage + place walk.
      DevCommands.register(dispatcher);
      // /cobblemon-initiative dev bot … — synthetic interactions + autobattle toggle
      // for Carpet fake players (acts on the SOURCE player; drive via execute as).
      DevBotCommand.register(dispatcher);
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

    // npcnote stick + pos capture + smoke checklist (registers its own callbacks).
    DevNoteInit.register();

    LOGGER.info("Dev tools initialized (gym-mark, npcnote/pos/smoke, dev subtree).");
  }

  public static GymMarkStorage getGymMarks() {
    return gymMarks;
  }
}

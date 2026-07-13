package com.thecompanyinc.cobblemoninitiative.devtools;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.DefaultActionResponse;
import com.cobblemon.mod.common.battles.ForcePassActionResponse;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.PassActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionRequest;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.SwitchActionResponse;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Dev-only auto-battler ({@code /cobblemon-initiative dev bot autobattle on|off}) —
 * makes headless (Carpet fake-player) battles PLAY OUT so post-victory chains
 * (prize pays once, badge→cap, defeat tags, onwin tokens) are verifiable without a
 * human at a client. Strips with the devtools package at 1.0.0 (TODO §2).
 *
 * <p>HOW IT SUBMITS (bytecode-verified against the pinned Cobblemon 1.7.3 jar): the real
 * client's choice packet lands in {@code BattleSelectActionsHandler.handle}, which does
 * exactly {@code battle.actors.find(player in getPlayerUUIDs).setActionResponses(list)}
 * gated on {@code actor.mustChoose} — on the server thread. Cobblemon's own AI actors use
 * the SAME entry point ({@code AIBattleActor.onChoiceRequested} →
 * {@code setActionResponses(request.iterate { battleAI.choose(…) })}). This tick hook
 * mirrors both: for each enrolled player whose actor has {@code mustChoose && request !=
 * null}, it builds one response per slot ({@code max(active.size, forceSwitch.size)},
 * matching {@code ShowdownActionRequest.iterate}) and calls
 * {@code BattleActor.setActionResponses(List&lt;ShowdownActionResponse&gt;)} from
 * {@code END_SERVER_TICK} — same call, same thread as the packet handler.
 *
 * <p>Choice policy = deterministic {@code RandomBattleAI} (first-legal instead of
 * random): forced switch → first {@code BattlePokemon.canBeSentOut()} party member;
 * otherwise first {@code InBattleMove.canBeUsed()} move, targeting the first
 * non-allied {@code Targetable} from {@code move.getTargets(slot)} (PNX), falling back
 * to struggle; pending capture/bag passes are honoured with
 * {@code ForcePassActionResponse} (so {@code dev bot useitem} ball throws mid-battle
 * keep the turn legal). An {@code IllegalActionChoiceException}
 * (extends IllegalArgumentException) downgrades the turn to all-PASS, exactly like the
 * AI's own fallback.
 */
public final class AutoBattler {

  /** Enrolled players — only ever the SOURCE player of the autobattle toggle. */
  private static final Set<UUID> ENROLLED = ConcurrentHashMap.newKeySet();

  private AutoBattler() {}

  /** Register the server-tick hook (called once from {@link DevToolsInit}). */
  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      if (ENROLLED.isEmpty()) return;
      for (UUID id : ENROLLED) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) tickPlayer(player);
      }
    });
    // Enrollment is session-only dev state — never persists.
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> ENROLLED.clear());
  }

  /** @return true if the enrollment state changed. */
  public static boolean setEnrolled(ServerPlayer player, boolean on) {
    return on ? ENROLLED.add(player.getUUID()) : ENROLLED.remove(player.getUUID());
  }

  // ---------------------------------------------------------------------------

  private static void tickPlayer(ServerPlayer player) {
    PokemonBattle battle = BattleRegistry.getBattleByParticipatingPlayer(player);
    if (battle == null || battle.getEnded()) return;
    BattleActor actor = battle.getActor(player);
    if (actor == null || !actor.getMustChoose()) return;
    ShowdownActionRequest request = actor.getRequest();
    if (request == null) return; // wait-turns leave mustChoose set with no request

    try {
      submit(actor, chooseAll(actor, request));
    } catch (IllegalActionChoiceException e) {
      DevToolsInit.LOGGER.warn(
        "[autobattle] {}: choice rejected ({}), passing the turn",
        player.getGameProfile().getName(), e.getMessage()
      );
      try {
        submit(actor, allPass(actor, request));
      } catch (IllegalActionChoiceException e2) {
        DevToolsInit.LOGGER.error(
          "[autobattle] {}: even PASS was rejected: {}",
          player.getGameProfile().getName(), e2.getMessage()
        );
      }
    } finally {
      // AIBattleActor.onChoiceRequested resets the switch marks right after submitting;
      // they only exist to keep a doubles turn from double-picking one candidate.
      for (BattlePokemon pokemon : actor.getPokemonList()) {
        pokemon.setWillBeSwitchedIn(false);
      }
    }
  }

  /**
   * Same call as {@code BattleSelectActionsHandler}: {@code actor.setActionResponses}.
   * A previous rejected submit leaves its already-validated entries in
   * {@code actor.responses} (Cobblemon appends before it throws), so drop those first —
   * {@code turn()} would clear them anyway on the next request.
   */
  private static void submit(BattleActor actor, List<ShowdownActionResponse> responses) {
    actor.getResponses().clear();
    actor.setActionResponses(responses);
  }

  /** One response per slot — size mirrors {@code ShowdownActionRequest.iterate}. */
  private static List<ShowdownActionResponse> chooseAll(
      BattleActor actor, ShowdownActionRequest request) {
    List<ShowdownMoveset> active = request.getActive();
    List<Boolean> forceSwitch = request.getForceSwitch();
    List<ActiveBattlePokemon> slots = actor.getActivePokemon();
    int n = Math.max(active == null ? 0 : active.size(), forceSwitch.size());
    // setActionResponses hard-returns past the slot list; never send more than it reads.
    n = Math.min(n, slots.size());

    // Pending capture/bag-item passes must be consumed by ForcePassActionResponse
    // entries (setActionResponses pops expectingPassActions in order and THROWS if any
    // are left over).
    int pendingPasses = actor.getExpectingPassActions().size();

    List<ShowdownActionResponse> out = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      ActiveBattlePokemon slot = slots.get(i);
      ShowdownMoveset moveset = (active != null && active.size() > i) ? active.get(i) : null;
      boolean mustSwitch = forceSwitch.size() > i && Boolean.TRUE.equals(forceSwitch.get(i));
      if (pendingPasses > 0 && !mustSwitch && moveset != null) {
        out.add(new ForcePassActionResponse());
        pendingPasses--;
      } else {
        out.add(chooseOne(actor, slot, moveset, mustSwitch));
      }
    }
    return out;
  }

  /** First-legal choice — {@code RandomBattleAI.choose} with first instead of random. */
  private static ShowdownActionResponse chooseOne(
      BattleActor actor, ActiveBattlePokemon slot, ShowdownMoveset moveset, boolean forceSwitch) {
    if (forceSwitch || slot.isGone()) {
      // canBeSentOut() already excludes fainted/sent-out/willBeSwitchedIn members.
      for (BattlePokemon candidate : actor.getPokemonList()) {
        if (candidate.canBeSentOut()) {
          candidate.setWillBeSwitchedIn(true); // mirrors RandomBattleAI (doubles safety)
          return new SwitchActionResponse(candidate.getUuid());
        }
      }
      return new DefaultActionResponse(); // nothing to switch to — mirror the AI
    }

    if (moveset == null) {
      return PassActionResponse.INSTANCE;
    }

    for (InBattleMove move : moveset.getMoves()) {
      if (!move.canBeUsed()) continue;
      // mustBeUsed (locked moves like Thrash) and no-target moves go out without a PNX.
      List<Targetable> targets = move.mustBeUsed() ? null : move.getTargets(slot);
      if (targets == null) {
        return new MoveActionResponse(move.getId(), null, null);
      }
      if (targets.isEmpty()) continue; // needs a target, none available
      Targetable chosen = targets.stream()
        .filter(t -> !t.isAllied(slot))
        .findFirst()
        .orElse(targets.get(0)); // default opponent slot; ally only if nothing else
      return new MoveActionResponse(move.getId(), chosen.getPNX(), null);
    }
    // All moves unusable — showdown expects struggle (same fallback as RandomBattleAI).
    return new MoveActionResponse("struggle", null, null);
  }

  /** The AI's exception fallback: pass every slot (ForcePass first where owed). */
  private static List<ShowdownActionResponse> allPass(
      BattleActor actor, ShowdownActionRequest request) {
    List<ShowdownMoveset> active = request.getActive();
    List<Boolean> forceSwitch = request.getForceSwitch();
    int n = Math.max(active == null ? 0 : active.size(), forceSwitch.size());
    n = Math.min(n, actor.getActivePokemon().size());

    int pendingPasses = actor.getExpectingPassActions().size();
    List<ShowdownActionResponse> out = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      ShowdownMoveset moveset = (active != null && active.size() > i) ? active.get(i) : null;
      boolean mustSwitch = forceSwitch.size() > i && Boolean.TRUE.equals(forceSwitch.get(i));
      if (pendingPasses > 0 && !mustSwitch && moveset != null) {
        out.add(new ForcePassActionResponse());
        pendingPasses--;
      } else {
        out.add(PassActionResponse.INSTANCE);
      }
    }
    return out;
  }
}

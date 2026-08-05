package com.thecompanyinc.cobblemoninitiative.phone;

import com.cobblemon.mod.common.battles.BattleRegistry;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.phone.PhoneCallScripts.PhoneCallScript;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * PokePhone ring-then-accept flow (0.7.0-alpha.20). Replaces the invisible-caller Easy NPC
 * dialog delivery: {@code /cobblemon-initiative phone ring <id>} starts a ring window
 * (ringtone + flashing actionbar); the player answers via keybind and the client screen
 * carries the call. The OWED-CALL contract: a missed, declined, aborted, or relogged-out
 * call is requeued and rings again after {@link PhoneCallConfig#reRingSeconds} — normal
 * COMPLETION is the only consume, so no story beat is ever lost. Content-side tick
 * functions keep calling {@code ring} until the script's done_tag lands; {@link #ring} is
 * idempotent per pending id, which is what makes that loop safe.
 *
 * <p>State is in-memory only (keyed by player UUID, kept across relogs within a session):
 * the datapack's done-tag-gated ring loop IS the persistence — a server restart just lets
 * it re-ring anything not yet completed.
 *
 * <p>Dev previews ({@link #ringPreview}) ride the whole flow — ring, answer, pages,
 * clickable choices — but completion skips commands and the done_tag, so a preview never
 * consumes the story beat.
 */
public class PhoneCallManager {

  /** Result of a ring request — the command layer turns these into feedback lines. */
  public enum RingResult { QUEUED, ALREADY_PENDING, UNKNOWN_ID }

  private static final class Pending {
    final String id;
    final boolean preview;
    int delayTicks;

    Pending(String id, boolean preview, int delayTicks) {
      this.id = id;
      this.preview = preview;
      this.delayTicks = delayTicks;
    }
  }

  private static final class Session {
    final PhoneCallScript script;
    final boolean preview;
    boolean answered;
    int ringTick;

    Session(PhoneCallScript script, boolean preview) {
      this.script = script;
      this.preview = preview;
    }
  }

  private static final class PlayerState {
    final Deque<Pending> queue = new ArrayDeque<>();
    Session active;
    int startRetryTicks; // battle-defer retry pacing (~1s between attempts)
  }

  private Map<String, PhoneCallScript> scripts = Map.of();
  private PhoneCallConfig config = new PhoneCallConfig();
  private final Map<UUID, PlayerState> states = new HashMap<>();

  /** SERVER_STARTED: scan every namespace's {@code phone_calls/} dir + (re)read the config. */
  public void onServerStarted(MinecraftServer server) {
    config = PhoneCallConfig.load();
    scripts = PhoneCallScripts.load(server);
  }

  /** The `phone reload` command — rescan scripts + config on the live server. */
  public void reload(MinecraftServer server) {
    onServerStarted(server);
  }

  /** ModMenu save path — timing knobs only, no rescan needed. */
  public void reloadConfig() {
    config = PhoneCallConfig.load();
  }

  public Collection<String> scriptIds() {
    return scripts.keySet();
  }

  public void registerEvents() {
    // A relog mid-call (either phase) is "without completing" — requeue silently. The
    // per-player state map deliberately keeps its entries for offline players.
    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
      PlayerState state = states.get(handler.player.getUUID());
      if (state != null && state.active != null) {
        requeue(state, state.active.script.id(), state.active.preview);
        state.active = null;
      }
    });
  }

  /** Ring the given player. Idempotent: a call already ringing or queued for them is a
   *  no-op (ALREADY_PENDING), so content tick loops may call this every cycle. */
  public RingResult ring(ServerPlayer player, String id) {
    return ring(player, id, false);
  }

  /**
   * Dev preview ring (/ca-dev phone): the full client UX, but completion skips choice
   * commands, on_complete, and the done_tag grant, so a stream preview never consumes the
   * story beat. Shares the pending-id dedupe with {@link #ring} — a preview colliding with
   * the real call is harmless either way, because a preview grants no done_tag and the
   * content tick loop just re-rings the real one afterwards.
   */
  public RingResult ringPreview(ServerPlayer player, String id) {
    return ring(player, id, true);
  }

  private RingResult ring(ServerPlayer player, String id, boolean preview) {
    PhoneCallScript script = scripts.get(id);
    if (script == null) return RingResult.UNKNOWN_ID;
    PlayerState state = states.computeIfAbsent(player.getUUID(), u -> new PlayerState());
    if (state.active != null && state.active.script.id().equals(id)) {
      return RingResult.ALREADY_PENDING;
    }
    for (Pending pending : state.queue) {
      if (pending.id.equals(id)) return RingResult.ALREADY_PENDING;
    }
    state.queue.addLast(new Pending(id, preview, 0));
    return RingResult.QUEUED;
  }

  /** END_SERVER_TICK: queue promotion (with the in-battle deferral), ring-window drive
   *  (ringtone pulses + flashing actionbar), and missed-call expiry/requeue. */
  public void tick(MinecraftServer server) {
    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      PlayerState state = states.get(player.getUUID());
      if (state == null) continue;

      for (Pending pending : state.queue) {
        if (pending.delayTicks > 0) pending.delayTicks--;
      }

      if (state.active == null) {
        tryStartRing(player, state);
      } else if (!state.active.answered) {
        tickRingWindow(player, state);
      }
      // An answered session is client-paced (typewriter/choices) — nothing to drive here;
      // completion, ESC-abort, and the disconnect hook are the only ways out.
    }
  }

  private void tryStartRing(ServerPlayer player, PlayerState state) {
    Pending head = state.queue.peekFirst();
    if (head == null || head.delayTicks > 0) return;
    if (state.startRetryTicks > 0) {
      state.startRetryTicks--;
      return;
    }
    // Never ring mid-battle — the phone screen pauses the game (integrated server), which
    // would freeze the showdown backlog under it. Retry ~each second until the battle ends.
    if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
      state.startRetryTicks = 20;
      return;
    }
    state.queue.pollFirst();
    PhoneCallScript script = scripts.get(head.id);
    if (script == null) {
      // Script vanished in a reload while queued — drop it; the content tick loop re-rings
      // if the call still exists under a new pack state.
      return;
    }
    state.active = new Session(script, head.preview);
    PhonePayloads.sendOffer(
      player, script.id(), script.caller(), script.subtitle(), script.avatar(),
      script.accent(), ringWindowTicks());
  }

  private void tickRingWindow(ServerPlayer player, PlayerState state) {
    Session session = state.active;
    session.ringTick++;

    // Two-tone amethyst ring: high chime then a lower echo, one pulse per second.
    int phase = session.ringTick % 20;
    if (phase == 1) {
      player.playNotifySound(
        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 1.7f);
    } else if (phase == 11) {
      player.playNotifySound(
        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7f, 1.275f);
    }

    // Flashing gold actionbar, alternating bright/dim every half second. [P] is the
    // shipped default binding for "Answer PokePhone" (the client owns any rebind).
    boolean bright = (session.ringTick / 10) % 2 == 0;
    String caller = session.script.caller();
    player.displayClientMessage(
      Component.literal(bright
        ? "§6§l☎ Incoming — " + caller + "  §r§e[P] to answer"
        : "§7☎ Incoming — " + caller + "  §8[P] to answer"),
      true);

    if (session.ringTick >= ringWindowTicks()) {
      state.active = null;
      requeue(state, session.script.id(), session.preview);
      player.displayClientMessage(
        Component.literal("§71 missed call — §f" + caller), true);
      player.playNotifySound(
        SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6f, 0.8f);
    }
  }

  /**
   * ANSWER (keybind ACCEPT click, or the dev hook with a null id). Replies with the full
   * script. Also heals the accept-after-missed race: if the id is no longer the active
   * ring but sits requeued, the player clearly wants the call — promote it immediately
   * (still never mid-battle), instead of leaving the client's splash waiting forever.
   */
  public boolean answer(ServerPlayer player, String callId) {
    PlayerState state = states.get(player.getUUID());
    if (state == null) return false;
    Session session = state.active;
    if (session != null && !session.answered
        && (callId == null || session.script.id().equals(callId))) {
      session.answered = true;
      openCall(player, session);
      return true;
    }
    if (callId != null && session == null) {
      for (Pending pending : state.queue) {
        if (pending.id.equals(callId)) {
          PhoneCallScript script = scripts.get(callId);
          if (script == null
              || BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            return false;
          }
          state.queue.remove(pending);
          state.active = new Session(script, pending.preview);
          state.active.answered = true;
          openCall(player, state.active);
          return true;
        }
      }
    }
    return false;
  }

  private void openCall(ServerPlayer player, Session session) {
    player.displayClientMessage(Component.empty(), true); // wipe the flashing ring line
    PhonePayloads.sendOpen(
      player, session.script.id(), session.script.pages(),
      session.script.choices().stream().map(PhoneCallScript.Choice::label).toList());
  }

  /** DECLINE from the splash — same requeue as a missed call (owed-call pattern). */
  public boolean decline(ServerPlayer player, String callId) {
    PlayerState state = states.get(player.getUUID());
    if (state == null || state.active == null || state.active.answered) return false;
    if (callId != null && !state.active.script.id().equals(callId)) return false;
    String caller = state.active.script.caller();
    requeue(state, state.active.script.id(), state.active.preview);
    state.active = null;
    player.displayClientMessage(
      Component.literal("§7Declined — §f" + caller + "§7 will call again."), true);
    return true;
  }

  /** CHOOSE(n) on the last page: run that choice's commands, then complete normally. */
  public boolean choose(ServerPlayer player, String callId, int choice) {
    PlayerState state = states.get(player.getUUID());
    if (state == null || state.active == null || !state.active.answered) return false;
    PhoneCallScript script = state.active.script;
    if (callId != null && !script.id().equals(callId)) return false;
    if (choice < 0 || choice >= script.choices().size()) {
      InitiativeInit.LOGGER.warn("[Phone] {} chose out-of-range index {} on call '{}'.",
        player.getName().getString(), choice, script.id());
      return false;
    }
    boolean preview = state.active.preview;
    state.active = null;
    completeCall(player, state, script, script.choices().get(choice), preview);
    return true;
  }

  /** COMPLETE (END CALL / dev hangup) — only valid for a script with NO choices; a
   *  choice script must resolve through {@link #choose}. */
  public boolean complete(ServerPlayer player, String callId) {
    PlayerState state = states.get(player.getUUID());
    if (state == null || state.active == null || !state.active.answered) return false;
    PhoneCallScript script = state.active.script;
    if (callId != null && !script.id().equals(callId)) return false;
    if (!script.choices().isEmpty()) return false;
    boolean preview = state.active.preview;
    state.active = null;
    completeCall(player, state, script, null, preview);
    return true;
  }

  /** ABORT (ESC on splash or mid-call) — the call was not completed, so it stays owed. */
  public boolean abort(ServerPlayer player, String callId) {
    PlayerState state = states.get(player.getUUID());
    if (state == null || state.active == null) return false;
    if (callId != null && !state.active.script.id().equals(callId)) return false;
    requeue(state, state.active.script.id(), state.active.preview);
    state.active = null;
    return true;
  }

  /**
   * Normal completion — the single consume: choice commands, then on_complete, then the
   * done_tag (identical names to the old Easy-NPC-era tags, so every phone/tick guard and
   * dialog gate keeps working). Commands are server-authored script data run AS the player
   * at perm 2, BARE form (the dialog-cmd lowering contract); dispatched directly because
   * both entry paths (packet receiver, tick) are plain dispatch — not nested in a command.
   * A preview session skips all of that and only announces itself.
   */
  private void completeCall(
    ServerPlayer player, PlayerState state, PhoneCallScript script,
    PhoneCallScript.Choice choice, boolean preview
  ) {
    if (preview) {
      // Stream-honest: the UX ran, the beat didn't — no commands, no done_tag, no consume.
      player.sendSystemMessage(Component.literal(
        "§7[preview] '" + script.id() + "' — side effects skipped (commands + done_tag not run)."));
      return;
    }
    MinecraftServer server = player.getServer();
    if (server == null) return;
    CommandSourceStack source =
      player.createCommandSourceStack().withPermission(2).withSuppressedOutput();
    if (choice != null) {
      for (String command : choice.commands()) {
        server.getCommands().performPrefixedCommand(source, command);
      }
    }
    for (String command : script.onComplete()) {
      server.getCommands().performPrefixedCommand(source, command);
    }
    if (script.doneTag() != null && !script.doneTag().isBlank()) {
      player.addTag(script.doneTag());
    }
    // Defensive: completion consumes every pending copy of this call, not just the session.
    state.queue.removeIf(pending -> pending.id.equals(script.id()));
  }

  private void requeue(PlayerState state, String id, boolean preview) {
    for (Pending pending : state.queue) {
      if (pending.id.equals(id)) {
        pending.delayTicks = reRingTicks();
        return;
      }
    }
    state.queue.addLast(new Pending(id, preview, reRingTicks()));
  }

  private int ringWindowTicks() {
    return Math.max(20, config.ringSeconds * 20);
  }

  private int reRingTicks() {
    return Math.max(20, config.reRingSeconds * 20);
  }
}

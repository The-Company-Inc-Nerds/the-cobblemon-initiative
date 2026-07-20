package com.thecompanyinc.cobblemoninitiative.nickname;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.network.InitiativePayloads;
import java.util.UUID;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * The nickname ritual: after any real acquisition — capture, gift, trade, starter —
 * the client is offered a name-entry prompt for the new teammate (showrunner feature,
 * 2026-07-19; in a Nuzlocke the name IS the stakes).
 *
 * <p>Two hooks cover every live acquisition path (recon-swept 2026-07-19):
 * <ul>
 *   <li>{@code POKEMON_CAPTURED} at {@link Priority#LOWEST} — all ball catches (wild,
 *       safari, noble body-swaps, shrine-crystal guardians). LOWEST so NuzlockeInit's
 *       NORMAL handler has already settled the dupes clause and PC routing; a released
 *       duplicate still reaches LOWEST subscribers, hence the storeCoordinates guard
 *       (the dupe release removes via StoreCoordinates, which nulls them — including
 *       for full-party catches that Cobblemon overflowed straight to the PC).</li>
 *   <li>{@code giveProperties()} in CobblemonInitiativeCommands — the single choke
 *       point for every scripted gift and the trade helper (givemon is the only live
 *       gift mechanism; givepokemonother has zero live call sites).</li>
 * </ul>
 * Deliberately NOT hooked: factory loaner grants/returns, daycare/momcare custody
 * returns, the Founder mirror — none are acquisitions, and none route through the two
 * hooks above.
 *
 * <p>The flow is fire-and-forget: the server sends the S2C offer and never waits.
 * A client that ESCs, crashes, or does not exist (Carpet bots) simply leaves the
 * species name standing. The C2S reply re-validates everything server-side.
 */
public final class NicknameManager {

  /**
   * Cobblemon's own cap (SetNicknameHandler.MAX_NAME_LENGTH in the 1.7.3 jar) — matched
   * so names set here obey the same limit as summary-screen renames.
   */
  public static final int MAX_NAME_LENGTH = 12;

  /**
   * Send-time suppression for the e2e harness: e2e_run's per-scenario setup runs
   * {@code tag <player> add ci_harness}, so givemon-heavy preambles (up to 30 in
   * one scenario) never stack modals on the driver. Production players never
   * carry it. (The literal command form above is also what registers the tag in
   * scenario_lint's corpus.)
   */
  public static final String SUPPRESS_TAG = "ci_harness";

  private NicknameManager() {}

  public static void registerEvents() {
    CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, event -> {
      Pokemon mon = event.getPokemon();
      // Null store coordinates ⇒ the dupes clause released it this tick — no prompt.
      if (mon.getStoreCoordinates().get() != null) {
        promptFor(event.getPlayer(), mon);
      }
      return Unit.INSTANCE;
    });
  }

  /**
   * Offer the prompt for a freshly-gained mon (safe to call before the store add —
   * the C2S reply resolves by uuid later). No-ops when the toggle is off, the player
   * is harness-tagged, or the mon arrived pre-named (givemon {@code nickname=…}).
   */
  public static void promptFor(ServerPlayer player, Pokemon mon) {
    if (!NuzlockeInit.getConfig().isNicknamePromptEnabled()) return;
    if (player.getTags().contains(SUPPRESS_TAG)) return;
    if (mon.getNickname() != null) return;
    InitiativePayloads.sendNicknamePrompt(player, mon.getUuid(), mon.getSpecies().getName());
  }

  /**
   * C2S resolution: sanitize, find the mon in THIS player's party or PC, name it.
   * {@code setNickname} syncs to the owning client and dirties the store on its own.
   * A stale uuid (released/traded since the offer) is silently dropped.
   */
  public static void applyNickname(ServerPlayer player, UUID monUuid, String rawName) {
    String name = sanitize(rawName);
    if (name.isEmpty()) return; // skip — the species name stands
    Pokemon mon = findOwned(player, monUuid);
    if (mon == null) return;
    // Direct set (deliberately NOT via Cobblemon's POKEMON_NICKNAMED event — that
    // event only fires on its SetNicknamePacket path, nothing in this pack
    // subscribes, and the pack is single-player; revisit if a listener appears).
    mon.setNickname(Component.literal(name));
    player.sendSystemMessage(Component.literal(
      "§d" + name + "§7 it is. The registry remembers."));
  }

  private static Pokemon findOwned(ServerPlayer player, UUID monUuid) {
    Pokemon mon = Cobblemon.INSTANCE.getStorage().getParty(player).get(monUuid);
    if (mon == null) mon = Cobblemon.INSTANCE.getStorage().getPC(player).get(monUuid);
    return mon;
  }

  /**
   * Strip formatting/control/bidi characters, trim, clamp to Cobblemon's cap.
   * Codepoint-aware: a legit client is capped by the EditBox, so this only guards
   * forged payloads — truncation must never split a surrogate pair.
   */
  private static String sanitize(String raw) {
    if (raw == null) return "";
    StringBuilder sb = new StringBuilder(raw.length());
    raw.codePoints().forEach(cp -> {
      if (cp == '§' || Character.isISOControl(cp)) return;
      if (Character.getType(cp) == Character.FORMAT) return;
      if (sb.length() + Character.charCount(cp) <= MAX_NAME_LENGTH + 8) sb.appendCodePoint(cp);
    });
    String s = sb.toString().trim();
    if (s.length() <= MAX_NAME_LENGTH) return s;
    int cut = MAX_NAME_LENGTH;
    if (Character.isHighSurrogate(s.charAt(cut - 1))) cut--;
    return s.substring(0, cut).trim();
  }
}

package com.thecompanyinc.cobblemoninitiative.mystic;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

/**
 * Titania's mirror ace (N10 playtest). When the marsh reads the challenger's lead (the
 * {@code reflect} command, run on dialog open), Titania's ACE is rebuilt as a clone of the
 * player's party slot-1 Pokémon, raised to the gym-3 leader ace level (Lv&nbsp;32 = entry cap
 * 30 + 2) and healed — so you fight your own strongest looking back at you across the field.
 *
 * <p>Same rctapi registry-swap mechanism as {@link com.thecompanyinc.cobblemoninitiative.founder.FounderMirrorManager}
 * (bytecode-verified public rctapi registry; the swap lands on the next {@code tbcs battle}, no
 * reload), but instead of replacing the whole team it reads each Titania variant's current team,
 * replaces only the highest-level mon (her signature ace — Gardevoir L32), and re-registers.
 * Injects into every Titania battle variant the dialog can route to, so whichever one fires
 * carries the mirror. Empty slot 1 or any failure leaves the currently-registered ace in place
 * (her static Gardevoir until the first mirror, then the last mirror injected) — since reflect
 * re-runs on every open, a non-empty re-open always re-mirrors to the current lead.
 */
public final class TitaniaAceManager {

  private TitaniaAceManager() {}

  /** Gym-3 entry cap (30) + 2 — the leader ace level (CLAUDE.md gym ladder). */
  public static final int ACE_LEVEL = 32;

  /** Every Titania battle trainer the mystic gym dialog can dispatch. */
  private static final List<String> TITANIA_TRAINER_IDS = List.of(
    "mystic_leader",
    "mystic_leader_weak",
    "mystic_leader_mirror_fire",
    "mystic_leader_mirror_water",
    "mystic_leader_mirror_grass",
    "mystic_leader_mirror_electric",
    "mystic_leader_mirror_shadow"
  );

  /**
   * Rebuild Titania's ace from the player's current party slot 1. Returns false (leaving her
   * own ace registered) on an empty lead or any failure.
   */
  public static boolean injectAce(ServerPlayer player) {
    if (player == null) return false;
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    Pokemon lead = party.get(0);
    if (lead == null) return false; // no lead -> Titania keeps her own Gardevoir

    Pokemon ace;
    try {
      ace = lead.clone(true, player.serverLevel().registryAccess());
      ace.setLevel(ACE_LEVEL);
      ace.heal();
    } catch (Throwable t) {
      InitiativeInit.LOGGER.warn("[TitaniaAce] Could not clone the lead — her own ace stands.", t);
      return false;
    }

    boolean any = false;
    for (String id : TITANIA_TRAINER_IDS) {
      any |= swapAce(player, id, ace);
    }
    if (any) {
      InitiativeInit.LOGGER.info(
        "[TitaniaAce] Titania's ace mirrored to {}'s lead ({} L{}).",
        player.getName().getString(), lead.getSpecies().getName(), ACE_LEVEL);
    }
    return any;
  }

  private static boolean swapAce(ServerPlayer player, String trainerId, Pokemon ace) {
    try {
      Object rct = Class.forName("com.gitlab.srcmc.rctmod.ModCommon").getField("RCT").get(null);
      Object registry = rct.getClass().getMethod("getTrainerRegistry").invoke(rct);
      Object current = registry.getClass()
        .getMethod("getById", String.class).invoke(registry, trainerId);
      if (current == null) return false;

      Pokemon[] team = (Pokemon[]) harvest(current, Pokemon[].class);
      if (team == null || team.length == 0) return false;

      // The ace = the highest-level mon (Gardevoir L32 in every variant); first on a tie.
      int aceIndex = 0;
      for (int i = 1; i < team.length; i++) {
        if (team[i] != null
            && (team[aceIndex] == null || team[i].getLevel() > team[aceIndex].getLevel())) {
          aceIndex = i;
        }
      }
      Pokemon[] newTeam = Arrays.copyOf(team, team.length);
      // Fresh clone per trainer — registerNPC's initTeam stamps team members, so no two
      // registry entries may share Pokemon instances.
      newTeam[aceIndex] = ace.clone(true, player.serverLevel().registryAccess());

      Object replacement = buildReplacement(current, newTeam);
      registry.getClass().getMethod("unregisterById", String.class).invoke(registry, trainerId);

      Method register = null;
      for (Method m : registry.getClass().getMethods()) {
        if (!"registerNPC".equals(m.getName()) || m.getParameterCount() != 2) continue;
        if (m.getParameterTypes()[1].isAssignableFrom(replacement.getClass())) {
          register = m;
          break;
        }
      }
      if (register == null) throw new NoSuchMethodException("registerNPC(String, TrainerNPC)");
      register.invoke(registry, trainerId, replacement);
      return true;
    } catch (Throwable t) {
      InitiativeInit.LOGGER.warn(
        "[TitaniaAce] Ace swap failed for {} — her own ace stands.", trainerId, t);
      return false;
    }
  }

  /**
   * Clone-construct the registry entry with a modified team: widest public constructor, each
   * parameter satisfied from the current entry's zero-arg getters, substituting our team for
   * the {@code Pokemon[]} slot. (Same shape as FounderMirrorManager#buildReplacement.)
   */
  private static Object buildReplacement(Object current, Pokemon[] team)
    throws ReflectiveOperationException {
    Constructor<?> widest = null;
    for (Constructor<?> ctor : current.getClass().getConstructors()) {
      if (widest == null || ctor.getParameterCount() > widest.getParameterCount()) {
        widest = ctor;
      }
    }
    if (widest == null) throw new NoSuchMethodException("no public TrainerNPC constructor");

    Class<?>[] params = widest.getParameterTypes();
    Object[] args = new Object[params.length];
    for (int i = 0; i < params.length; i++) {
      Class<?> param = params[i];
      if (param.isArray() && param.getComponentType().isAssignableFrom(Pokemon.class)) {
        args[i] = team;
        continue;
      }
      args[i] = harvest(current, param);
    }
    return widest.newInstance(args);
  }

  /** First zero-arg getter on the entry whose return type satisfies {@code wanted}. */
  private static Object harvest(Object source, Class<?> wanted)
    throws ReflectiveOperationException {
    for (Method m : source.getClass().getMethods()) {
      if (m.getParameterCount() != 0) continue;
      if (!m.getName().startsWith("get")) continue;
      if (wanted.isAssignableFrom(m.getReturnType()) && !m.getReturnType().equals(Object.class)) {
        return m.invoke(source);
      }
    }
    return null;
  }
}

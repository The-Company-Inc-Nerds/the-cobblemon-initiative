package com.thecompanyinc.cobblemoninitiative.founder;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.server.level.ServerPlayer;

/**
 * The Founder party-mirror: regenerates the {@code villain_final_boss} trainer from the
 * player's LIVE Cobblemon party (each mon cloned, healed, raised to level 100), so the
 * mirror fight is literally the player's own team looking back. The static
 * {@code data/rctmod/trainers/villain_final_boss.json} shadow team remains the fallback
 * whenever a refresh has not run or fails.
 *
 * <p>Mechanism (rctapi 0.15.2 bytecode-verified, 2026-07-18): rctmod's parsed trainers are
 * registered on the PUBLIC rctapi registry ({@code ModCommon.RCT.getTrainerRegistry()},
 * {@code registerNPC}/{@code unregisterById}/{@code getById} all public). TBCS keeps its own
 * registry in sync by LISTENING to that registry's register/unregister events (it deep-copies
 * the entry under {@code rctmod:<id>}), and battle teams are cloned from the registry entry
 * AT BATTLE-BUILD TIME — so an unregister+register swap here takes effect on the very next
 * {@code tbcs battle}, with no datapack reload of any kind. rctapi is runtime-only (not a
 * compile dep), hence reflection — but every touched member is public, so no setAccessible.
 *
 * <p>Register throws on an existing id ({@code putIfAbsent} + explicit throw), so the swap
 * MUST unregister first. {@code registerNPC} runs {@code initTeam} on the passed team
 * (original-trainer stamp + uncatchable), which is why the mirror clones the party instead
 * of handing over the live Pokémon objects.
 *
 * <p>Triggers: board_cleared grant (the moment the fight unlocks), player join while
 * board_cleared is held (keeps the mirror fresh across sessions), the Founder's own
 * "Face myself" dialog button, and {@code /cobblemon-initiative mirror refresh}.
 */
public final class FounderMirrorManager {

  public static final String MIRROR_TRAINER_ID = "villain_final_boss";
  public static final int MIRROR_LEVEL = 100;

  private FounderMirrorManager() {}

  /**
   * Rebuild the mirror from the player's current party. Returns false (and leaves the
   * static shadow team registered) on any failure — an unplayable Founder is worse than
   * a stale one, so every error path degrades to "the last registered team stands".
   */
  public static boolean refresh(ServerPlayer player) {
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    java.util.List<Pokemon> mirror = new java.util.ArrayList<>();
    for (int i = 0; i < 6; i++) {
      Pokemon source = party.get(i);
      if (source == null) continue;
      Pokemon clone = source.clone(true, player.serverLevel().registryAccess());
      clone.setLevel(MIRROR_LEVEL);
      clone.heal();
      mirror.add(clone);
    }
    if (mirror.isEmpty()) {
      InitiativeInit.LOGGER.warn(
        "[FounderMirror] {} has an empty party — mirror refresh skipped.",
        player.getName().getString());
      return false;
    }

    try {
      Object rct = Class.forName("com.gitlab.srcmc.rctmod.ModCommon")
        .getField("RCT").get(null);
      Object registry = rct.getClass().getMethod("getTrainerRegistry").invoke(rct);
      Object current = registry.getClass()
        .getMethod("getById", String.class).invoke(registry, MIRROR_TRAINER_ID);
      if (current == null) {
        InitiativeInit.LOGGER.warn(
          "[FounderMirror] {} is not in the rctmod trainer registry — refresh skipped.",
          MIRROR_TRAINER_ID);
        return false;
      }

      Object replacement = buildReplacement(current, mirror.toArray(new Pokemon[0]));

      registry.getClass().getMethod("unregisterById", String.class)
        .invoke(registry, MIRROR_TRAINER_ID);
      // Select the registerNPC overload matching the concrete TrainerNPC (the other
      // overload takes a TrainerModel).
      Method register = null;
      for (Method m : registry.getClass().getMethods()) {
        if (!"registerNPC".equals(m.getName()) || m.getParameterCount() != 2) continue;
        if (m.getParameterTypes()[1].isAssignableFrom(replacement.getClass())) {
          register = m;
          break;
        }
      }
      if (register == null) {
        throw new NoSuchMethodException("registerNPC(String, TrainerNPC)");
      }
      register.invoke(registry, MIRROR_TRAINER_ID, replacement);

      InitiativeInit.LOGGER.info(
        "[FounderMirror] Rebuilt {} as {}'s level-{} mirror ({} mon).",
        MIRROR_TRAINER_ID, player.getName().getString(), MIRROR_LEVEL, mirror.size());
      return true;
    } catch (Throwable t) {
      InitiativeInit.LOGGER.warn(
        "[FounderMirror] Mirror refresh failed — the static shadow team stands.", t);
      return false;
    }
  }

  /**
   * Clone-construct the registry entry with the mirror team: pick the widest public
   * constructor of the live TrainerNPC and satisfy each parameter from the current
   * entry's zero-arg getters (every ctor parameter type is distinct — Text, Pokemon[],
   * GimmicksMap, TrainerBag, ResourceLocation, BattleAI, LivingEntity — so type-matched
   * harvesting is unambiguous), substituting our team for the Pokemon[] slot.
   */
  private static Object buildReplacement(Object current, Pokemon[] mirrorTeam)
    throws ReflectiveOperationException {
    Constructor<?> widest = null;
    for (Constructor<?> ctor : current.getClass().getConstructors()) {
      if (widest == null || ctor.getParameterCount() > widest.getParameterCount()) {
        widest = ctor;
      }
    }
    if (widest == null) {
      throw new NoSuchMethodException("no public TrainerNPC constructor");
    }

    Class<?>[] params = widest.getParameterTypes();
    Object[] args = new Object[params.length];
    for (int i = 0; i < params.length; i++) {
      Class<?> param = params[i];
      if (param.isArray() && param.getComponentType().isAssignableFrom(Pokemon.class)) {
        args[i] = mirrorTeam;
        continue;
      }
      args[i] = harvest(current, param);
    }
    return widest.newInstance(args);
  }

  /** First zero-arg getter on the entry whose return type satisfies the parameter. */
  private static Object harvest(Object source, Class<?> wanted)
    throws ReflectiveOperationException {
    for (Method m : source.getClass().getMethods()) {
      if (m.getParameterCount() != 0) continue;
      if (!m.getName().startsWith("get")) continue;
      if (wanted.isAssignableFrom(m.getReturnType()) && !m.getReturnType().equals(Object.class)) {
        return m.invoke(source);
      }
    }
    // Nullable slots (e.g. the attached LivingEntity before any body exists) fall
    // through to null rather than failing the whole refresh.
    return null;
  }
}

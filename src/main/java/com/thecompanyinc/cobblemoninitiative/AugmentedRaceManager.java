package com.thecompanyinc.cobblemoninitiative;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Drives the Cyber City AUGMENTED RING RUN (sidequest/augmented_race) — a 37-ring vertical parkour
 * race started from Arlo Datagear's rooftop rig. The datapack owns the timer, the ordered rings, the
 * bossbar and the win/fail split; this manager adds the two things a datapack cannot express:
 *
 * <ol>
 *   <li><b>The augment</b> — Speed II + Jump Boost II ("values of 1 and 1" = amplifier 1) re-asserted
 *       every server tick for any player carrying the {@code ci_aug_racing} tag, so a mid-run relog
 *       re-arms the buff. When the tag drops (win / expire / floor-fail / relog-abandon — all handled
 *       in the datapack) the manager strips both effects on the next tick so the augment never leaks
 *       past a run. {@code start.mcfunction} also applies the effects once for immediacy.</li>
 *   <li><b>The crouch-land roll</b> — a datapack selector cannot test is_sneaking and a datapack
 *       cannot write a player's fall distance, so fall-damage negation MUST be Java. While a racing
 *       player is shift-key-down, an incoming FALL-type hit is cancelled and their fall distance is
 *       reset (the proven idiom: {@code player.resetFallDistance()} in CyclopsManager /
 *       ShrineChallengeManager; the cancel seam is the same ServerLivingEntityEvents family
 *       DojoKnockoutManager uses ALLOW_DEATH from).</li>
 * </ol>
 *
 * <p>Static manager (mirrors {@link DojoKnockoutManager}/{@link CyclopsManager}); {@link #init()} is
 * called from {@code InitiativeInit.onInitialize}.
 */
public final class AugmentedRaceManager {

  private AugmentedRaceManager() {}

  /** The datapack's single source of truth for "this player is running the augment race." */
  private static final String RACING_TAG = "ci_aug_racing";

  /** Speed amplifier 5 = Speed VI ("hard to control" — pushed higher on the 2026-08-06 follow-up;
   *  was 3/Speed IV, originally 1/Speed II). Jump stays amplifier 1 (Jump Boost II). Tune here. */
  private static final int SPEED_AMPLIFIER = 5;
  private static final int JUMP_AMPLIFIER = 1;

  /** Re-asserted each tick, so a comfortably-longer-than-one-tick duration keeps the buff steady
   *  without a visible flicker; dropping the tag lets it lapse (belt to the explicit clear below). */
  private static final int EFFECT_DURATION_TICKS = 40;

  public static void init() {
    ServerLivingEntityEvents.ALLOW_DAMAGE.register(AugmentedRaceManager::onAllowDamage);
    ServerTickEvents.END_SERVER_TICK.register(AugmentedRaceManager::tick);
  }

  /** Crouch-on-land roll: cancel a FALL hit for a racing player who is holding crouch, and clear the
   *  residual fall distance so the very next tick's descent does not immediately re-deal it. */
  private static boolean onAllowDamage(LivingEntity entity, DamageSource source, float amount) {
    if (!(entity instanceof ServerPlayer sp)) return true;
    if (!sp.getTags().contains(RACING_TAG)) return true;
    if (!source.is(DamageTypeTags.IS_FALL)) return true;
    if (!sp.isShiftKeyDown()) return true;
    sp.resetFallDistance();
    return false; // cancelled — the roll absorbs the landing
  }

  /** Re-assert the augment for every racing player; strip it from anyone still buffed but no longer
   *  racing (the tag was removed by the datapack this tick). */
  private static void tick(MinecraftServer server) {
    for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
      if (sp.getTags().contains(RACING_TAG)) {
        // ambient + no icon: showIcon=false keeps the HUD clean; the effects are the augment, not loot.
        sp.addEffect(new MobEffectInstance(
          MobEffects.MOVEMENT_SPEED, EFFECT_DURATION_TICKS, SPEED_AMPLIFIER, false, false, false));
        sp.addEffect(new MobEffectInstance(
          MobEffects.JUMP, EFFECT_DURATION_TICKS, JUMP_AMPLIFIER, false, false, false));
      } else if (sp.hasEffect(MobEffects.MOVEMENT_SPEED) || sp.hasEffect(MobEffects.JUMP)) {
        // Only the augment ever grants these two at once via this manager; the datapack win/fail paths
        // already `effect clear` for immediacy, so this is a safety net for a tag dropped by relog.
        sp.removeEffect(MobEffects.MOVEMENT_SPEED);
        sp.removeEffect(MobEffects.JUMP);
      }
    }
  }
}

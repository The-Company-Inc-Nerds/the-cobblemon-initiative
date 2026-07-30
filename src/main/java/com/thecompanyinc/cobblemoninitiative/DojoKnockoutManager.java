package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.config.DojoConfig;
import java.util.Locale;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Non-lethal Deepcore dojo (gym 4). When knockout mode is on ({@link DojoConfig#isKnockoutMode()}):
 * <ul>
 *   <li>A defeated dojo fighter is left as a body lying at the spot instead of just vanishing —
 *       a {@code dojo_knocked} body is imported at the death position (the kill still credits the
 *       floor via the existing absence-poll).</li>
 *   <li>A player who would be killed BY A DOJO FIGHTER is knocked out instead: dropped to
 *       {@link DojoConfig#getKnockoutPlayerHealth()} HP, charged {@link DojoConfig#getKnockoutCost()}
 *       CobbleDollars, and given a few seconds of immunity so a swarming fighter cannot instantly
 *       re-drop them.</li>
 * </ul>
 * All three knobs are ModMenu-tunable (see DojoConfig). Only DOJO-fighter damage knocks the player
 * out — a whiteout, a fall, or lava still kills, so the Nuzlocke stakes are untouched elsewhere.
 */
public final class DojoKnockoutManager {

  private DojoKnockoutManager() {}

  private static final String KNOCKED_PRESET = "easy_npc:preset/humanoid/dojo_knocked.npc.snbt";

  public static void init() {
    ServerLivingEntityEvents.ALLOW_DEATH.register(DojoKnockoutManager::onAllowDeath);
    ServerLivingEntityEvents.AFTER_DEATH.register(DojoKnockoutManager::onAfterDeath);
  }

  /** Cancel a player's death when a dojo fighter dealt the killing blow — knock them out instead. */
  private static boolean onAllowDeath(LivingEntity entity, DamageSource source, float amount) {
    if (!(entity instanceof ServerPlayer sp) || sp.getAbilities().instabuild) return true;
    DojoConfig cfg = DojoConfig.get();
    if (!cfg.isKnockoutMode()) return true;
    if (!(source.getEntity() instanceof LivingEntity killer) || !isDojoHostile(killer)) return true;
    knockOutPlayer(sp, cfg);
    return false; // cancelled — the player lives
  }

  /** Leave a knocked-out body where a dojo fighter fell. */
  private static void onAfterDeath(LivingEntity entity, DamageSource source) {
    if (entity instanceof Player) return;
    if (!DojoConfig.get().isKnockoutMode()) return;
    if (!isDojoHostile(entity)) return;
    if (!(entity.level() instanceof ServerLevel sl)) return;
    double x = entity.getX(), y = entity.getY(), z = entity.getZ();
    sl.getServer().getCommands().performPrefixedCommand(
      sl.getServer().createCommandSourceStack().withPosition(entity.position())
        .withPermission(2).withSuppressedOutput(),
      String.format(Locale.ROOT, "easy_npc preset import_new data %s %.2f %.2f %.2f",
        KNOCKED_PRESET, x, y, z));
    sl.sendParticles(ParticleTypes.CRIT, x, y + 1.0, z, 25, 0.3, 0.4, 0.3, 0.1);
    sl.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
      SoundSource.HOSTILE, 1.0f, 0.7f);
  }

  private static boolean isDojoHostile(LivingEntity e) {
    for (String t : e.getTags()) {
      if (t.startsWith("dc_") && t.endsWith("_hostile")) return true;
    }
    return false;
  }

  private static void knockOutPlayer(ServerPlayer sp, DojoConfig cfg) {
    sp.setHealth(cfg.getKnockoutPlayerHealth()); // already clamped >= 0.5 by the config getter
    sp.clearFire();
    // Brief full immunity so a nearby fighter cannot instantly re-drop them into a CD-drain loop.
    sp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, false, false));
    sp.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false));
    int cost = cfg.getKnockoutCost();
    if (cost > 0) {
      sp.getServer().getCommands().performPrefixedCommand(
        sp.getServer().createCommandSourceStack().withSuppressedOutput().withPermission(2),
        "cobbledollars remove " + sp.getScoreboardName() + " " + cost);
    }
    sp.level().playSound(null, sp.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
      SoundSource.PLAYERS, 1.0f, 0.8f);
    sp.displayClientMessage(Component.literal(
      "§4Knocked out! §7The dojo takes " + cost
        + " CobbleDollars and drags you off the mat — still breathing."), false);
  }
}

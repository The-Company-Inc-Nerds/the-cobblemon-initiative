package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.config.DojoConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Non-lethal Deepcore dojo (gym 4). When knockout mode is on ({@link DojoConfig#isKnockoutMode()}):
 * <ul>
 *   <li>A defeated dojo fighter is left as a body lying at the spot instead of just vanishing —
 *       the fighter's OWN {@code dojo_knocked_*} body (same name + skin, a-22 playtest note 1) is
 *       imported at the death position (the kill still credits the floor via the existing
 *       absence-poll).</li>
 *   <li>A player who would be killed BY A DOJO FIGHTER is knocked out instead: dropped to
 *       {@link DojoConfig#getKnockoutPlayerHealth()} HP, charged {@link DojoConfig#getKnockoutCost()}
 *       CobbleDollars, and given a few seconds of immunity so a swarming fighter cannot instantly
 *       re-drop them. With {@link DojoConfig#isResetOnKnockout()} (a-22 playtest note 2, default on)
 *       the KO also ejects them to the quarry-side clinic and fully resets the dojo run.</li>
 * </ul>
 * All knobs are ModMenu-tunable (see DojoConfig). Only DOJO-fighter damage knocks the player
 * out — a whiteout, a fall, or lava still kills, so the Nuzlocke stakes are untouched elsewhere.
 */
public final class DojoKnockoutManager {

  private DojoKnockoutManager() {}

  /** Legacy one-size-fits-all sleeper — kept for saves that predate the per-character bodies and
   *  as the fallback for any future {@code dc_*_hostile} tag missing from the map below. */
  private static final String KNOCKED_PRESET_FALLBACK =
    "easy_npc:preset/humanoid/dojo_knocked.npc.snbt";

  /** Hostile duel-body tag -> its own knocked-out sleeper preset (same name + skin; the sleeper
   *  presets are compiled from dialog-src/characters/deepcore/dojo_knocked_*.json). Full string
   *  literals — dialog_lint scans Java literals. */
  private static final Map<String, String> KNOCKED_PRESET_BY_TAG = Map.of(
    "dc_floor_1_hostile", "easy_npc:preset/humanoid/dojo_knocked_1.npc.snbt",
    "dc_floor_2_hostile", "easy_npc:preset/humanoid/dojo_knocked_2.npc.snbt",
    "dc_floor_3_hostile", "easy_npc:preset/humanoid/dojo_knocked_3.npc.snbt",
    "dc_floor_4_hostile", "easy_npc:preset/humanoid/dojo_knocked_4.npc.snbt",
    "dc_striker_hostile", "easy_npc:preset/humanoid/dojo_knocked_striker.npc.snbt",
    "dc_ken_hostile", "easy_npc:preset/humanoid/dojo_knocked_ken.npc.snbt");

  /** Every live hostile the KO reset must remove — via {@link Entity#discard()}, which fires no
   *  death event: a /kill in the reset function would re-enter {@link #onAfterDeath} and drop a
   *  fresh corpse mid-reset. */
  private static final Set<String> HOSTILE_TAGS = Set.of(
    "dc_floor_1_hostile", "dc_floor_2_hostile", "dc_floor_3_hostile", "dc_floor_4_hostile",
    "dc_striker_hostile", "dc_ken_hostile");

  /** Rilka's quarry-side clinic post — the eject faces the KO'd player at her. */
  private static final double EJECT_FACE_X = 1092.5;
  private static final double EJECT_FACE_Y = 114.0;
  private static final double EJECT_FACE_Z = 3208.5;

  /** KO'd players queued for the eject+reset. The ALLOW_DEATH callback fires mid damage
   *  application — teleporting or sweeping entities there is unsafe, so the fallout is drained on
   *  the next END_SERVER_TICK plain-tick path (PENDING_CONFIRMS idiom, UtilityFeeManager). The
   *  dojo chunks are still loaded then: the player is on the mat until the drain teleports them. */
  private static final List<UUID> PENDING_EJECTS =
    java.util.Collections.synchronizedList(new ArrayList<>());

  public static void init() {
    ServerLivingEntityEvents.ALLOW_DEATH.register(DojoKnockoutManager::onAllowDeath);
    ServerLivingEntityEvents.AFTER_DEATH.register(DojoKnockoutManager::onAfterDeath);
    ServerTickEvents.END_SERVER_TICK.register(DojoKnockoutManager::tick);
  }

  /** Cancel a player's death when a dojo fighter dealt the killing blow — knock them out instead. */
  private static boolean onAllowDeath(LivingEntity entity, DamageSource source, float amount) {
    if (!(entity instanceof ServerPlayer sp) || sp.getAbilities().instabuild) return true;
    DojoConfig cfg = DojoConfig.get();
    if (!cfg.isKnockoutMode()) return true;
    if (!(source.getEntity() instanceof LivingEntity killer) || dojoHostileTag(killer) == null) {
      return true;
    }
    knockOutPlayer(sp, cfg);
    return false; // cancelled — the player lives
  }

  /** Leave the fallen fighter's own knocked-out body where it fell. */
  private static void onAfterDeath(LivingEntity entity, DamageSource source) {
    if (entity instanceof Player) return;
    if (!DojoConfig.get().isKnockoutMode()) return;
    String tag = dojoHostileTag(entity);
    if (tag == null) return;
    if (!(entity.level() instanceof ServerLevel sl)) return;
    double x = entity.getX(), y = entity.getY(), z = entity.getZ();
    sl.getServer().getCommands().performPrefixedCommand(
      sl.getServer().createCommandSourceStack().withPosition(entity.position())
        .withPermission(2).withSuppressedOutput(),
      String.format(Locale.ROOT, "easy_npc preset import_new data %s %.2f %.2f %.2f",
        KNOCKED_PRESET_BY_TAG.getOrDefault(tag, KNOCKED_PRESET_FALLBACK), x, y, z));
    sl.sendParticles(ParticleTypes.CRIT, x, y + 1.0, z, 25, 0.3, 0.4, 0.3, 0.1);
    sl.playSound(null, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
      SoundSource.HOSTILE, 1.0f, 0.7f);
  }

  /** The entity's {@code dc_*_hostile} tag, or null when it is not a dojo fighter. */
  private static String dojoHostileTag(Entity e) {
    for (String t : e.getTags()) {
      if (t.startsWith("dc_") && t.endsWith("_hostile")) return t;
    }
    return null;
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
    if (cfg.isResetOnKnockout()) {
      PENDING_EJECTS.add(sp.getUUID());
      sp.displayClientMessage(Component.literal(
        "§4Knocked out! §7The dojo takes " + cost
          + " CobbleDollars — you wake by the quarry-side clinic. The dojo has reset."), false);
    } else {
      sp.displayClientMessage(Component.literal(
        "§4Knocked out! §7The dojo takes " + cost
          + " CobbleDollars and drags you off the mat — still breathing."), false);
    }
  }

  /** Drains queued KOs: reset the dojo FIRST (while the KO'd player still holds its chunks
   *  loaded), THEN eject them to the clinic. */
  private static void tick(MinecraftServer server) {
    if (PENDING_EJECTS.isEmpty()) return;
    List<UUID> batch;
    synchronized (PENDING_EJECTS) {
      batch = new ArrayList<>(PENDING_EJECTS);
      PENDING_EJECTS.clear();
    }
    for (UUID id : batch) {
      // Live hostiles out via discard() (no death event — see HOSTILE_TAGS), then the datapack
      // reset (corpse sweep, latches, defeat tags, passive re-arm). Runs even if the player
      // logged out mid-tick: the chunks are still loaded on the disconnect tick.
      List<Entity> hostiles = new ArrayList<>();
      for (ServerLevel lvl : server.getAllLevels()) {
        for (Entity e : lvl.getAllEntities()) {
          for (String t : e.getTags()) {
            if (HOSTILE_TAGS.contains(t)) { hostiles.add(e); break; }
          }
        }
      }
      hostiles.forEach(Entity::discard);
      server.getCommands().performPrefixedCommand(
        server.createCommandSourceStack().withPermission(2).withSuppressedOutput(),
        "function cobblemon_initiative:gym/dojo_reset");
      ServerPlayer sp = server.getPlayerList().getPlayer(id);
      if (sp == null) continue;
      DojoConfig cfg = DojoConfig.get();
      // `tp facing` for the wake-up orientation (toward Rilka); the command loads the target chunk.
      server.getCommands().performPrefixedCommand(
        server.createCommandSourceStack().withPermission(2).withSuppressedOutput(),
        String.format(Locale.ROOT, "tp %s %.2f %.2f %.2f facing %.2f %.2f %.2f",
          sp.getScoreboardName(), cfg.getDojoEjectX(), cfg.getDojoEjectY(), cfg.getDojoEjectZ(),
          EJECT_FACE_X, EJECT_FACE_Y, EJECT_FACE_Z));
    }
  }
}

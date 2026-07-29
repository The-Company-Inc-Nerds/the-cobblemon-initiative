package com.thecompanyinc.cobblemoninitiative;

import com.thecompanyinc.cobblemoninitiative.config.DojoConfig;
import com.thecompanyinc.cobblemoninitiative.config.OrcConfig;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Applies the ModMenu difficulty multipliers to the datapack-spawned PVP bodies:
 * <ul>
 *   <li>Deepcore dojo hostiles (tag {@code dc_*_hostile}) ← {@link DojoConfig}</li>
 *   <li>rotating orc-camp bodies (tag {@code ci_orc}) ← {@link OrcConfig}</li>
 * </ul>
 * Those bodies are spawned by {@code easy_npc preset import_new} from static duel-snippet presets,
 * so their baked max_health / attack_damage are scaled here in Java — ONCE per body, marked with a
 * {@code dojo_scaled}/{@code orc_scaled} tag so a chunk reload never double-scales. Timing mirrors
 * {@code NpcPresetRefreshManager}: queue on ENTITY_LOAD, apply one tick later. Also pushes the orc
 * {@code spoilsRolls} reward knob to the {@code #cfg_orc_spoils_rolls ci_ambient} scoreboard (read by
 * {@code orc/camp_cleared}) on server start and after a ModMenu save.
 */
public final class DojoDifficultyManager {

  private static final Set<UUID> incoming = new LinkedHashSet<>();
  private static final Set<UUID> ready = new LinkedHashSet<>();
  private static boolean initialized;

  private DojoDifficultyManager() {}

  public static void init() {
    if (initialized) return;
    initialized = true;
    ServerEntityEvents.ENTITY_LOAD.register(DojoDifficultyManager::onEntityLoad);
    ServerTickEvents.END_SERVER_TICK.register(DojoDifficultyManager::tick);
    ServerLifecycleEvents.SERVER_STARTED.register(DojoDifficultyManager::pushOrcSpoils);
  }

  private static boolean hasTag(Entity entity, String exact) {
    return entity.getTags().contains(exact);
  }

  private static boolean isDojoHostile(Entity entity) {
    for (String tag : entity.getTags()) {
      if (tag.startsWith("dc_") && tag.endsWith("_hostile")) return true;
    }
    return false;
  }

  private static void onEntityLoad(Entity entity, ServerLevel level) {
    if (!(entity instanceof LivingEntity)) return;
    if (isDojoHostile(entity) && !hasTag(entity, "dojo_scaled")) { incoming.add(entity.getUUID()); return; }
    if (hasTag(entity, "ci_orc") && !hasTag(entity, "orc_scaled")) incoming.add(entity.getUUID());
  }

  private static void applyMultipliers(LivingEntity le, float hMult, float dMult, String mark) {
    AttributeInstance maxHealth = le.getAttribute(Attributes.MAX_HEALTH);
    if (maxHealth != null && hMult != 1.0f) {
      maxHealth.setBaseValue(maxHealth.getBaseValue() * hMult);
    }
    AttributeInstance attackDamage = le.getAttribute(Attributes.ATTACK_DAMAGE);
    if (attackDamage != null && dMult != 1.0f) {
      attackDamage.setBaseValue(attackDamage.getBaseValue() * dMult);
    }
    le.setHealth(le.getMaxHealth());
    le.addTag(mark);
  }

  private static void tick(MinecraftServer server) {
    if (!ready.isEmpty()) {
      DojoConfig dojo = DojoConfig.get();
      OrcConfig orc = OrcConfig.get();
      for (UUID uuid : ready) {
        Entity entity = resolve(server, uuid);
        if (!(entity instanceof LivingEntity le)) continue;
        if (isDojoHostile(le) && !le.getTags().contains("dojo_scaled")) {
          applyMultipliers(le, dojo.getFighterHealthMultiplier(), dojo.getFighterDamageMultiplier(), "dojo_scaled");
        } else if (le.getTags().contains("ci_orc") && !le.getTags().contains("orc_scaled")) {
          applyMultipliers(le, orc.getHealthMultiplier(), orc.getDamageMultiplier(), "orc_scaled");
        }
      }
      ready.clear();
    }
    if (!incoming.isEmpty()) {
      ready.addAll(incoming);
      incoming.clear();
    }
  }

  private static Entity resolve(MinecraftServer server, UUID uuid) {
    for (ServerLevel level : server.getAllLevels()) {
      Entity entity = level.getEntity(uuid);
      if (entity != null) return entity;
    }
    return null;
  }

  /** Publish the OrcConfig reward knob to the scoreboard orc/camp_cleared reads. */
  public static void pushOrcSpoils(MinecraftServer server) {
    if (server == null) return;
    var source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    server.getCommands().performPrefixedCommand(source, "scoreboard objectives add ci_ambient dummy");
    server.getCommands().performPrefixedCommand(source,
      "scoreboard players set #cfg_orc_spoils_rolls ci_ambient " + OrcConfig.get().getSpoilsRolls());
  }
}

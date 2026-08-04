package com.thecompanyinc.cobblemoninitiative.wisp;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Wisp-Lantern set-piece (Mystic Marsh). Bryn's keepsake — a soul lantern carrying the
 * {@code ci_wisp_lantern} marker — cannot be placed anywhere ordinary. Held up at the gym's WEST
 * GATE after dark, the wisps rise and LEAD the player (a bright guide light) through the marsh to
 * the Sunken Ship. Placed at that one spot, the lantern lights, a cutscene plays, and a <b>Marshadow
 * (Lv&nbsp;35)</b> emerges — an interact-to-join gift (dialog {@code marshadow_gift}).
 */
public final class WispLanternManager {

  private WispLanternManager() {}

  private static final String MARKER = "ci_wisp_lantern";

  /** Guide origin — the gym's west gate (where Bryn sends the player). */
  private static final Vec3 LEAD_START = new Vec3(899.5, 69.0, 2439.5);
  /** The one spot the lantern will rest (the Sunken Ship — matches the cutscene focus). */
  private static final Vec3 SPOT = new Vec3(741.5, 64.0, 2502.5);
  private static final double SPOT_TOL_H = 6.0;
  private static final double SPOT_TOL_Y = 5.0;
  /** Where the Marshadow gift body is imported (playtest pin 2026-08-03 P1 — the re-recorded
   *  lantern_reveal camera settles looking at this exact spot; decimal coords, do not floor). */
  private static final Vec3 MARSHADOW_POS = new Vec3(740.4, 62.0, 2508.4);
  private static final String MARSHADOW_PRESET =
    "easy_npc:preset/humanoid/marshadow_lantern.npc.snbt";

  /** The lead shows this far from the spot (covers west gate -> ship). */
  private static final double GUIDE_RANGE = 220.0;
  private static final double LEAD_DISTANCE = 6.0;
  private static final int GUIDE_PERIOD = 4;

  private static final String TAG_PLACED = "marsh_lantern_placed";
  private static final String TAG_JOINED = "marshadow_joined";

  // ── Placement handler (UseBlockCallback) ─────────────────────────────────────────

  public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
    if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
    ItemStack stack = player.getItemInHand(hand);
    if (!isWispLantern(stack)) return InteractionResult.PASS;

    // Once the shadow has joined, the lantern is just a lantern again.
    if (sp.getTags().contains(TAG_JOINED)) return InteractionResult.PASS;

    Vec3 place = Vec3.atCenterOf(hit.getBlockPos());
    boolean atSpot = Math.abs(place.x - SPOT.x) <= SPOT_TOL_H
                  && Math.abs(place.z - SPOT.z) <= SPOT_TOL_H
                  && Math.abs(place.y - SPOT.y) <= SPOT_TOL_Y;

    if (!atSpot) {
      hint(sp, "The lantern will not rest here. Take it to the gym's west gate after dark and follow the wisps.");
      return InteractionResult.FAIL; // block placement everywhere but the spot
    }
    if (sp.getTags().contains(TAG_PLACED)) {
      hint(sp, "The lantern is already lit. The shadow is waiting where the wisps gathered.");
      return InteractionResult.SUCCESS;
    }
    if (!isNight(serverLevel)) {
      hint(sp, "The wisp-lantern hangs quiet. The marsh only answers it after dark.");
      return InteractionResult.SUCCESS; // keep the lantern
    }

    placeAndReveal(serverLevel, sp, hit, hand);
    return InteractionResult.SUCCESS;
  }

  private static void placeAndReveal(ServerLevel level, ServerPlayer sp, BlockHitResult hit, InteractionHand hand) {
    sp.addTag(TAG_PLACED);

    // Rest the lantern on the aimed face (guaranteed placeable — they clicked a solid block).
    BlockPos lanternPos = hit.getBlockPos().relative(hit.getDirection());
    if (level.getBlockState(lanternPos).canBeReplaced()) {
      level.setBlockAndUpdate(lanternPos, Blocks.SOUL_LANTERN.defaultBlockState());
    }
    sp.getItemInHand(hand).shrink(1);

    revealEffect(level);
    runAsPlayer(sp, "cutscene play lantern_reveal");
    spawnMarshadow(level);
    InitiativeInit.LOGGER.info("[WispLantern] {} lit the lantern — Marshadow revealed.", sp.getName().getString());
  }

  private static void spawnMarshadow(ServerLevel level) {
    level.getServer().getCommands().performPrefixedCommand(
      level.getServer().createCommandSourceStack().withPosition(MARSHADOW_POS).withPermission(2).withSuppressedOutput(),
      "easy_npc preset import_new data " + MARSHADOW_PRESET
        + " " + MARSHADOW_POS.x + " " + MARSHADOW_POS.y + " " + MARSHADOW_POS.z);
  }

  private static void revealEffect(ServerLevel level) {
    double x = MARSHADOW_POS.x, y = MARSHADOW_POS.y + 0.6, z = MARSHADOW_POS.z;
    BlockPos soundPos = BlockPos.containing(MARSHADOW_POS);
    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 90, 1.2, 1.0, 1.2, 0.02);
    level.sendParticles(ParticleTypes.SOUL, x, y, z, 60, 1.0, 1.2, 1.0, 0.02);
    level.sendParticles(ParticleTypes.END_ROD, x, y + 0.5, z, 40, 0.8, 1.0, 0.8, 0.05);
    level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 40, 0.9, 0.7, 0.9, 0.01);
    level.playSound(null, soundPos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 1.2f, 0.5f);
    level.playSound(null, soundPos, SoundEvents.WARDEN_AMBIENT, SoundSource.HOSTILE, 0.7f, 1.4f);
    level.playSound(null, soundPos, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.8f, 0.7f);
  }

  // ── The lead / guide light (per-player tick) ─────────────────────────────────────

  public static void guideTick(ServerPlayer sp) {
    if (sp.getTags().contains(TAG_JOINED) || sp.getTags().contains(TAG_PLACED)) return;
    if (!holdsWispLantern(sp)) return;
    ServerLevel level = sp.serverLevel();
    if (!isNight(level)) return;
    if (sp.position().distanceToSqr(SPOT) > GUIDE_RANGE * GUIDE_RANGE) return;
    long t = level.getGameTime();
    if (t % GUIDE_PERIOD != 0) return;

    Vec3 eye = sp.getEyePosition();
    double dx = SPOT.x - sp.getX(), dz = SPOT.z - sp.getZ();
    double distH = Math.sqrt(dx * dx + dz * dz);
    Vec3 guide;
    if (distH <= LEAD_DISTANCE) {
      guide = SPOT.add(0.0, 1.0, 0.0); // converge on the spot
    } else {
      double nx = dx / distH, nz = dz / distH;
      guide = new Vec3(eye.x + nx * LEAD_DISTANCE, eye.y + 0.3, eye.z + nz * LEAD_DISTANCE);
    }
    double bob = Math.sin(t * 0.2) * 0.3;
    double gy = guide.y + bob;
    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, guide.x, gy, guide.z, 4, 0.15, 0.2, 0.15, 0.0);
    level.sendParticles(ParticleTypes.END_ROD, guide.x, gy, guide.z, 2, 0.1, 0.1, 0.1, 0.0);
    level.sendParticles(ParticleTypes.SOUL, guide.x, gy, guide.z, 2, 0.2, 0.2, 0.2, 0.005);
    if (t % 40 == 0) {
      level.playSound(null, BlockPos.containing(guide), SoundEvents.CANDLE_AMBIENT,
        SoundSource.AMBIENT, 0.5f, 1.5f);
    }
  }

  // ── helpers ──────────────────────────────────────────────────────────────────────

  private static boolean isWispLantern(ItemStack stack) {
    if (stack.isEmpty()) return false;
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.copyTag().getBoolean(MARKER);
  }

  private static boolean holdsWispLantern(ServerPlayer sp) {
    return isWispLantern(sp.getMainHandItem()) || isWispLantern(sp.getOffhandItem());
  }

  private static boolean isNight(ServerLevel level) {
    long t = level.getDayTime() % 24000L;
    return t >= 13000L && t <= 23000L;
  }

  private static void runAsPlayer(ServerPlayer sp, String cmd) {
    sp.getServer().getCommands().performPrefixedCommand(
      sp.createCommandSourceStack().withPermission(2).withSuppressedOutput(), cmd);
  }

  private static void hint(ServerPlayer sp, String msg) {
    sp.displayClientMessage(Component.literal("§7" + msg), true);
  }
}

package com.thecompanyinc.cobblemoninitiative.economy;

import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/**
 * The Company charges to use its PUBLIC town workstations — brewing stands, enchanting tables,
 * furnaces, crafting tables, anvils, and the rest — a fee that rises with badge count, to nudge the
 * player into building (and powering) their own base outside town. Storage and passage blocks
 * (chests, barrels, doors, buttons, beds, …) stay free.
 *
 * <p>Only fires inside a {@code TOWN} safe zone; creative players are exempt. Because there is no
 * Java CobbleDollars API (charging is command-rail only, and a balance read is async), this
 * drains the fee on open rather than hard-gating — a broke player still gets in (CobbleDollars
 * {@code remove} clamps at 0), which is fine: the point is the drain, not a lockout. A short
 * per-block cooldown keeps re-opening the same station from re-charging.
 */
public final class UtilityFeeManager {

  private UtilityFeeManager() {}

  /** Chargeable public workstations. Storage/passage/redstone blocks are deliberately excluded. */
  private static final java.util.Set<Block> UTILITY_BLOCKS = java.util.Set.of(
    Blocks.BREWING_STAND, Blocks.ENCHANTING_TABLE,
    Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER,
    Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL,
    Blocks.GRINDSTONE, Blocks.STONECUTTER, Blocks.LOOM,
    Blocks.CARTOGRAPHY_TABLE, Blocks.SMITHING_TABLE
  );

  private static final int FEE_BASE = 20;
  private static final int FEE_PER_BADGE = 15;
  /** Re-opening the same station within this many ticks is free (avoids per-click double-charge). */
  private static final long COOLDOWN_TICKS = 1200L;

  /** key = playerUUID + ":" + blockPos.asLong() -> last-charged game time. */
  private static final Map<String, Long> LAST_CHARGED = new HashMap<>();

  public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
    if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
    if (sp.getAbilities().instabuild) return InteractionResult.PASS; // creative: free
    NuzlockeConfig cfg = NuzlockeInit.getConfig();
    if (cfg == null) return InteractionResult.PASS;

    BlockPos pos = hit.getBlockPos();
    BlockState state = level.getBlockState(pos);
    if (!UTILITY_BLOCKS.contains(state.getBlock())) return InteractionResult.PASS;

    // Sneaking with an item = the player is placing/using that item on the block, not opening it.
    if (sp.isSecondaryUseActive() && !sp.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;

    MinecraftServer server = sp.getServer();
    String town = cfg.townZoneNameAt(
      level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ(), server);
    if (town == null) return InteractionResult.PASS; // your own base outside town: free

    long now = sp.level().getGameTime();
    String key = sp.getUUID() + ":" + pos.asLong();
    Long last = LAST_CHARGED.get(key);
    if (last != null && now - last < COOLDOWN_TICKS) return InteractionResult.PASS; // still paid up
    LAST_CHARGED.put(key, now);

    int fee = FEE_BASE + FEE_PER_BADGE * badges(sp);
    // Fire-and-forget deduction (remove clamps at 0 — a broke player is drained to empty, not blocked).
    server.getCommands().performPrefixedCommand(
      server.createCommandSourceStack().withSuppressedOutput().withPermission(2),
      "cobbledollars remove " + sp.getScoreboardName() + " " + fee);
    sp.displayClientMessage(Component.literal(
      "§6The Company bills you §e" + fee + "§6 to use its public equipment. §7Build your own to skip the fee."), true);
    return InteractionResult.PASS; // let the station open normally
  }

  private static int badges(ServerPlayer player) {
    try {
      Scoreboard sb = player.getServer().getScoreboard();
      Objective obj = sb.getObjective("memory_fragment");
      if (obj == null) return 0;
      ReadOnlyScoreInfo info =
        sb.getPlayerScoreInfo(ScoreHolder.forNameOnly(player.getScoreboardName()), obj);
      return info != null ? Math.max(0, info.value()) : 0;
    } catch (Exception e) {
      return 0;
    }
  }
}

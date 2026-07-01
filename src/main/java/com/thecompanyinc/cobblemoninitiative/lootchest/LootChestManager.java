package com.thecompanyinc.cobblemoninitiative.lootchest;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.LootChestConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Turns chests the player did not place themselves into progress-scaled loot
 * dispensers.
 *
 * A chest is "player-placed" only if it was placed by a player's hand (tracked
 * via {@code Block.setPlacedBy}). Chests that ship with the map — placed by
 * structures, worldedit, or {@code /setblock} — are NOT tracked, so opening one
 * suppresses the normal chest screen and grants a few random items from the
 * {@code badge_reward} loot tables for the player's badge tier instead.
 *
 * The 10 gym badges map directly to loot tiers 0..10 (0 badges = tier_0).
 */
public class LootChestManager {

  private static final int MAX_TIER = 10;

  private final LootChestStorage storage = new LootChestStorage();

  // ── Lifecycle ─────────────────────────────────────────────────────────────────

  public void load(MinecraftServer server) {
    storage.load(server);
  }

  public void save() {
    storage.save();
  }

  public LootChestStorage getStorage() {
    return storage;
  }

  // ── Event hooks ───────────────────────────────────────────────────────────────

  /** Called from the {@code setPlacedBy} mixin when a player places a chest. */
  public void onChestPlacedByPlayer(BlockPos pos) {
    storage.markPlayerPlaced(pos);
  }

  /** Called on block break — forget tracking so the position can be reused. */
  public void onBlockBroken(BlockState state, BlockPos pos) {
    if (state.getBlock() instanceof ChestBlock) {
      storage.forget(pos);
    }
  }

  /**
   * {@code UseBlockCallback} handler. Returns {@link InteractionResult#SUCCESS}
   * to suppress the vanilla chest screen when we dispense loot; otherwise PASS.
   */
  public InteractionResult onChestUse(
    Player player,
    Level level,
    InteractionHand hand,
    BlockHitResult hit
  ) {
    if (level.isClientSide()) return InteractionResult.PASS;
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

    LootChestConfig config = LootChestConfig.get();
    if (!config.isEnabled()) return InteractionResult.PASS;

    // Sneaking = placing a block / other interaction, not opening the chest.
    if (player.isShiftKeyDown()) return InteractionResult.PASS;

    BlockPos pos = hit.getBlockPos();
    BlockState state = level.getBlockState(pos);
    if (!(state.getBlock() instanceof ChestBlock)) return InteractionResult.PASS;

    BlockPos partner = chestPartner(pos, state);

    // The player placed this chest — leave it as a normal container.
    if (storage.isPlayerPlaced(pos) ||
        (partner != null && storage.isPlayerPlaced(partner))) {
      return InteractionResult.PASS;
    }

    // Already looted once — fall through to the (now normal) chest.
    if (config.isOneTimePerChest() &&
        (storage.isClaimed(pos) || (partner != null && storage.isClaimed(partner)))) {
      return InteractionResult.PASS;
    }

    // Mark claimed (and persist) BEFORE granting, so a crash mid-grant can't
    // re-roll the chest. All tier loot tables are present, so the grant won't fail.
    if (config.isOneTimePerChest()) {
      storage.markClaimed(pos);
      if (partner != null) storage.markClaimed(partner);
    }

    // Unplaced, unclaimed chest → dispense progress-scaled loot instead of opening.
    grantLoot(serverPlayer);

    serverPlayer.serverLevel().playSound(
      null,
      pos,
      SoundEvents.CHEST_OPEN,
      SoundSource.BLOCKS,
      0.9f,
      1.0f
    );

    return InteractionResult.SUCCESS;
  }

  // ── Loot ───────────────────────────────────────────────────────────────────────

  private void grantLoot(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return;

    int tier = badgeTier(player);
    LootChestConfig config = LootChestConfig.get();

    // Suppress the /loot "Gave items to …" feedback (source is the server).
    CommandSourceStack source = server
      .createCommandSourceStack()
      .withPermission(4)
      .withSuppressedOutput();
    String name = player.getName().getString();

    if (config.isGiveCobblemonPool()) {
      server.getCommands().performPrefixedCommand(
        source,
        "loot give " + name + " loot cobblemon_initiative:badge_reward/cobblemon/tier_" + tier
      );
    }
    if (config.isGiveMinecraftPool()) {
      server.getCommands().performPrefixedCommand(
        source,
        "loot give " + name + " loot cobblemon_initiative:badge_reward/minecraft/tier_" + tier
      );
    }

    player.sendSystemMessage(
      Component.literal(
        "§6§l[Supply Cache] §r§7Someone stocked this before you arrived. You take what looks useful."
      )
    );

    InitiativeInit.LOGGER.debug(
      "Granted unplaced-chest loot (tier {}) to {}",
      tier,
      name
    );
  }

  /** Number of gym badges earned, clamped to the available loot tiers (0..10). */
  private int badgeTier(ServerPlayer player) {
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);
    int badges = 0;
    for (LevelCapConfig cap : InitiativeInit.getConfigLoader().getLevelCaps()) {
      String id = cap.getAchievementId();
      if (id != null && id.startsWith("badge_") && progress.hasAchievement(id)) {
        badges++;
      }
    }
    return Math.min(badges, MAX_TIER);
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  /** The other half of a double chest, or null if this is a single chest. */
  private static BlockPos chestPartner(BlockPos pos, BlockState state) {
    if (state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) return null;
    return pos.relative(ChestBlock.getConnectedDirection(state));
  }
}

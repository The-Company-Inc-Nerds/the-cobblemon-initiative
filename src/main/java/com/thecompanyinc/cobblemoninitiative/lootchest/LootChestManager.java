package com.thecompanyinc.cobblemoninitiative.lootchest;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.LootChestConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Turns chests the player did not place themselves into progress-scaled loot
 * caches.
 *
 * A chest is "player-placed" only if it was placed by a player's hand (tracked
 * via {@code Block.setPlacedBy}). Chests that ship with the map — placed by
 * structures, worldedit, or {@code /setblock} — are NOT tracked, so the first
 * time one is opened it is stocked in place with a few random items from the
 * {@code badge_reward} loot tables for the player's badge tier, and then opens
 * normally so the player takes what they want straight from the chest.
 *
 * The 10 gym badges map directly to loot tiers 0..10 (0 badges = tier_0).
 */
public class LootChestManager {

  private static final int MAX_TIER = 10;
  private static final String NAMESPACE = "cobblemon_initiative";

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
   * {@code UseBlockCallback} handler. Always returns {@link InteractionResult#PASS}
   * so the vanilla chest screen opens — but on the first open of an unplaced,
   * unclaimed chest it first stocks the chest's own inventory with loot.
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

    // Already stocked once — just open the (now normal) chest.
    if (config.isOneTimePerChest() &&
        (storage.isClaimed(pos) || (partner != null && storage.isClaimed(partner)))) {
      return InteractionResult.PASS;
    }

    // Mark claimed (and persist) BEFORE stocking, so a crash mid-stock can't
    // re-roll the chest. All tier loot tables are present, so the roll won't fail.
    if (config.isOneTimePerChest()) {
      storage.markClaimed(pos);
      if (partner != null) storage.markClaimed(partner);
    }

    // Stock the chest in place, then PASS so vanilla opens the now-filled chest.
    stockChest(serverPlayer, state, level, pos, partner);
    return InteractionResult.PASS;
  }

  // ── Loot ───────────────────────────────────────────────────────────────────────

  /** Roll the enabled pools for the player's tier and scatter the result into the chest. */
  private void stockChest(
    ServerPlayer player,
    BlockState state,
    Level level,
    BlockPos pos,
    BlockPos partner
  ) {
    List<ItemStack> loot = rollLoot(player);
    if (loot.isEmpty()) return;

    // Both halves of a double chest, so items can fill the whole 54-slot view.
    List<Container> halves = new ArrayList<>();
    if (level.getBlockEntity(pos) instanceof Container c0) halves.add(c0);
    if (partner != null && level.getBlockEntity(partner) instanceof Container c1) halves.add(c1);
    if (halves.isEmpty()) return;

    // Collect every empty slot across the halves as {halfIndex, slot}, then scatter.
    List<int[]> emptySlots = new ArrayList<>();
    for (int h = 0; h < halves.size(); h++) {
      Container c = halves.get(h);
      for (int s = 0; s < c.getContainerSize(); s++) {
        if (c.getItem(s).isEmpty()) emptySlots.add(new int[] { h, s });
      }
    }
    Collections.shuffle(emptySlots);

    int placed = 0;
    for (ItemStack stack : loot) {
      if (emptySlots.isEmpty()) break; // chest full
      if (stack.isEmpty()) continue;
      int[] slot = emptySlots.remove(emptySlots.size() - 1);
      halves.get(slot[0]).setItem(slot[1], stack);
      placed++;
    }
    for (Container c : halves) c.setChanged();

    if (placed > 0) {
      player.sendSystemMessage(
        Component.literal(
          "§6§l[Supply Cache] §r§7Someone stocked this before you arrived."
        )
      );
      InitiativeInit.LOGGER.debug(
        "Stocked unplaced chest at {} with {} item stack(s) (tier {}) for {}",
        pos,
        placed,
        badgeTier(player),
        player.getName().getString()
      );
    }
  }

  /** Items rolled from the enabled pools for the player's tier, scaled by the multiplier. */
  private List<ItemStack> rollLoot(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return List.of();

    ServerLevel level = player.serverLevel();
    int tier = badgeTier(player);
    LootChestConfig config = LootChestConfig.get();

    List<ItemStack> rolled = new ArrayList<>();
    if (config.isGiveMinecraftPool()) {
      rolled.addAll(rollTable(server, level, player, "badge_reward/minecraft/tier_" + tier));
    }
    if (config.isGiveCobblemonPool()) {
      rolled.addAll(rollTable(server, level, player, "badge_reward/cobblemon/tier_" + tier));
    }

    // Scale each stack's count by the multiplier (1.0 = default). Counts above a
    // stack's max size overflow into additional stacks; a 0 multiplier yields none.
    double mult = config.getLootMultiplier();
    List<ItemStack> scaled = new ArrayList<>();
    for (ItemStack stack : rolled) {
      if (stack.isEmpty()) continue;
      long count = Math.round(stack.getCount() * mult);
      int maxSize = stack.getMaxStackSize();
      while (count > 0) {
        int take = (int) Math.min(count, maxSize);
        ItemStack copy = stack.copy();
        copy.setCount(take);
        scaled.add(copy);
        count -= take;
      }
    }
    return scaled;
  }

  private List<ItemStack> rollTable(
    MinecraftServer server,
    ServerLevel level,
    ServerPlayer player,
    String path
  ) {
    ResourceKey<LootTable> key = ResourceKey.create(
      Registries.LOOT_TABLE,
      ResourceLocation.fromNamespaceAndPath(NAMESPACE, path)
    );
    LootTable table = server.reloadableRegistries().getLootTable(key);
    LootParams params = new LootParams.Builder(level)
      .withParameter(LootContextParams.ORIGIN, player.position())
      .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
      .create(LootContextParamSets.CHEST);
    return table.getRandomItems(params);
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

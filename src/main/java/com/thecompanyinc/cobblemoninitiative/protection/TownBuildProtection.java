package com.thecompanyinc.cobblemoninitiative.protection;

import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Town build-lock: inside a {@code TOWN}-type safe zone, players may not PLACE or BREAK
 * blocks, but every block INTERACTION (chests, doors, buttons, furnaces, NPCs, …) still
 * works. This keeps the curated UPM 2 towns from being griefed / accidentally reshaped on
 * a hardcore playthrough while leaving normal town life intact.
 *
 * <p>Placement is cancelled by {@code BlockItemMixin} (a HEAD hook on {@code BlockItem.place},
 * which fires only on an actual placement — after vanilla has already resolved block
 * interaction priority — so we never interfere with opening a chest while holding a stack of
 * blocks). Breaking is cancelled via {@code PlayerBlockBreakEvents.BEFORE}. Fluid buckets are
 * caught here in a {@code UseBlockCallback} (a filled bucket changes the world without going
 * through {@code BlockItem.place}).
 *
 * <p>Creative-mode ({@code instabuild}) players are exempt so the map can still be edited in
 * dev. The whole feature is gated on {@link NuzlockeConfig#isEnableTownBuildProtection()} and
 * {@link NuzlockeConfig#isEnableSafeZones()}.
 */
public final class TownBuildProtection {

  private TownBuildProtection() {}

  /** Per-player throttle so a held-down click doesn't spam the actionbar notice. */
  private static final Map<UUID, Long> LAST_NOTICE = new HashMap<>();
  private static final long NOTICE_COOLDOWN_TICKS = 30L;

  /** Whether the build-lock is live for this world right now (server-side, both toggles on). */
  public static boolean active(Level level) {
    if (level == null || level.isClientSide()) return false;
    NuzlockeConfig cfg = NuzlockeInit.getConfig();
    return cfg != null && cfg.isEnableSafeZones() && cfg.isEnableTownBuildProtection();
  }

  /** Creative-mode (instabuild) players keep full build rights for map editing. */
  public static boolean exempt(Player player) {
    return player == null || player.getAbilities().instabuild;
  }

  /** The active TOWN zone name containing {@code pos}, or null if the position is not in a town. */
  public static String townAt(Level level, BlockPos pos) {
    NuzlockeConfig cfg = NuzlockeInit.getConfig();
    if (cfg == null) return null;
    return cfg.townZoneNameAt(
      level.dimension().location().toString(),
      pos.getX(), pos.getY(), pos.getZ(),
      level.getServer());
  }

  /** {@code PlayerBlockBreakEvents.BEFORE} handler — returns false to cancel the break. */
  public static boolean onBlockBreak(Level level, Player player, BlockPos pos, BlockState state) {
    if (!active(level) || exempt(player)) return true;
    String town = townAt(level, pos);
    if (town == null) return true;
    notify(player, town, "break");
    return false;
  }

  /**
   * {@code UseItemCallback} handler for FLUID BUCKETS. A filled/empty bucket acts via
   * {@code BucketItem.use} (its own raycast — the item-use path), NOT the block-click path, so
   * {@code UseBlockCallback} never catches it and a {@code FAIL} there is ignored anyway (the client
   * still runs {@code useItem}); this is the only hook that stops buckets. Fire items go through the
   * separate block hook below, and block placement proper through {@code BlockItemMixin}.
   */
  public static InteractionResultHolder<ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (!active(level) || exempt(player)) return InteractionResultHolder.pass(stack);
    if (!(stack.getItem() instanceof BucketItem)) return InteractionResultHolder.pass(stack);
    // The player stands inside the town they are altering (buckets reach ~5 blocks — a town-wide
    // lock does not need the exact fluid cell).
    String town = townAt(level, player.blockPosition());
    if (town == null) return InteractionResultHolder.pass(stack);
    notify(player, town, "build");
    resync(player);
    return InteractionResultHolder.fail(stack);
  }

  /**
   * {@code UseBlockCallback} handler for FIRE items. Flint-and-steel and fire charges light fires via
   * {@code Item.useOn} (the block-click path), which dispatches {@code UseBlockCallback}; a
   * {@code FAIL} here cancels them server-side. (Buckets do NOT go through {@code useOn}, which is
   * why they are handled on {@code UseItemCallback} above.)
   */
  public static InteractionResult onUseBlockFire(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
    if (!active(level) || exempt(player)) return InteractionResult.PASS;
    Item item = player.getItemInHand(hand).getItem();
    if (!(item instanceof FlintAndSteelItem || item instanceof FireChargeItem)) return InteractionResult.PASS;
    String town = townAt(level, hit.getBlockPos());
    if (town == null) return InteractionResult.PASS;
    notify(player, town, "build");
    return InteractionResult.FAIL;
  }

  /**
   * Called from {@code BlockItemMixin} when a block would be placed. Returns true when the
   * placement lands in a protected town (and the placer is not exempt) so the mixin can
   * cancel it.
   */
  public static boolean blocksPlacement(Player player, Level level, BlockPos placePos) {
    if (!active(level) || exempt(player)) return false;
    String town = townAt(level, placePos);
    if (town == null) return false;
    notify(player, town, "build");
    resync(player);
    return true;
  }

  private static void resync(Player player) {
    if (player instanceof ServerPlayer sp) {
      sp.containerMenu.sendAllDataToRemote();
    }
  }

  private static void notify(Player player, String town, String verb) {
    if (!(player instanceof ServerPlayer sp)) return;
    long now = sp.level().getGameTime();
    Long last = LAST_NOTICE.get(sp.getUUID());
    if (last != null && now - last < NOTICE_COOLDOWN_TICKS) return;
    LAST_NOTICE.put(sp.getUUID(), now);
    String msg = "break".equals(verb)
      ? "§7You cannot break blocks in " + town + " — this is a protected town."
      : "§7You cannot build in " + town + " — this is a protected town.";
    sp.displayClientMessage(Component.literal(msg), true);
  }
}

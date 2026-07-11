package com.thecompanyinc.cobblemoninitiative.fieldmark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The GymMark WAND — the zero-typing flow for the gym-gimmick coordinate pass.
 *
 * <p>An amethyst shard whose NAME always says what you are marking (and, for boxes,
 * which corner). Right-click a block once to tentatively mark it (particle preview +
 * actionbar), right-click the SAME block again to confirm — the wand records the mark,
 * renames itself to the next unmarked slot, and moves on. Boxes take two confirmed
 * corners (the {@code start}/{@code stop} pair, walked in order). Right-click AIR to
 * mark your own feet instead (for floating spots like the rift origin/crystals —
 * fly there and double-click the sky). Sneak + right-click skips to the next slot
 * without marking.
 *
 * <p>Everything lands in the same per-world storage as the {@code gym-mark} commands
 * ({@code data/gym_marks.json}); {@code gym-mark list}/{@code export} work unchanged.
 * Get the wand with {@code /cobblemon-initiative gym-mark wand}. Dev-only — ships and
 * dies with the field-mark tool (see the dev-only cleanup checklist).
 */
public final class GymMarkWand {

  private static final String NAME_PREFIX = "GymMark";
  /** Second click must land within this many ticks to confirm. */
  private static final long CONFIRM_WINDOW_TICKS = 200;

  private static GymMarkStorage storage;

  /** Per-player wand cursor: which slot the wand is currently offering. */
  private static final Map<UUID, String> currentSlot = new HashMap<>();

  /** Per-player pending (first) click awaiting its confirming twin. */
  private static final Map<UUID, Pending> pending = new HashMap<>();

  private record Pending(String slot, boolean secondCorner, BlockPos pos, long gameTime) {}

  private GymMarkWand() {}

  // ── Wiring ───────────────────────────────────────────────────────────────────

  public static void registerEvents(GymMarkStorage stor) {
    storage = stor;

    // Right-click a BLOCK: mark that block (precise wall corners).
    UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
      if (world.isClientSide || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
      ItemStack held = player.getItemInHand(hand);
      if (!isWand(held)) return InteractionResult.PASS;
      if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
      handleClick(sp, held, ((BlockHitResult) hitResult).getBlockPos());
      return InteractionResult.SUCCESS;
    });

    // Right-click AIR: mark the player's own feet (floating spots — rift origin, crystals).
    UseItemCallback.EVENT.register((player, world, hand) -> {
      ItemStack held = player.getItemInHand(hand);
      if (world.isClientSide || hand != InteractionHand.MAIN_HAND || !isWand(held)) {
        return InteractionResultHolder.pass(held);
      }
      if (player instanceof ServerPlayer sp) {
        handleClick(sp, held, sp.blockPosition());
        return InteractionResultHolder.success(held);
      }
      return InteractionResultHolder.pass(held);
    });
  }

  /** /cobblemon-initiative gym-mark wand — hand over a wand aimed at the first open slot. */
  public static void give(ServerPlayer player, GymMarkStorage stor) {
    storage = stor;
    ItemStack wand = new ItemStack(Items.AMETHYST_SHARD);
    // The wand is identified by a CUSTOM_DATA marker, NOT its display name — the name
    // carries color codes ("§bGymMark…"), so a prefix test on getString() never matches,
    // and custom data also survives anvil renames.
    CompoundTag marker = new CompoundTag();
    marker.putBoolean("ci_gym_wand", true);
    wand.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(marker));
    String slot = advanceTo(player.getUUID(), firstOpenSlot(null));
    rename(wand, player.getUUID());
    wand.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(List.of(
      Component.literal("§7Right-click a block once to preview, again to confirm."),
      Component.literal("§7Right-click AIR to mark your own feet (fly for sky spots)."),
      Component.literal("§7Sneak + right-click skips to the next slot."),
      Component.literal("§8gym-mark list / export when done.")
    )));
    player.getInventory().add(wand);
    player.sendSystemMessage(Component.literal(
      "§a[GymMark] §7Wand ready — now marking §f" + (slot == null ? "(all done!)" : slot) + "§7."));
  }

  // ── Click handling ───────────────────────────────────────────────────────────

  private static void handleClick(ServerPlayer sp, ItemStack wand, BlockPos pos) {
    UUID id = sp.getUUID();

    if (sp.isShiftKeyDown()) { // skip to the next slot
      pending.remove(id);
      String next = advanceTo(id, nextSlotAfter(currentSlot.get(id)));
      rename(wand, id);
      actionbar(sp, next == null
        ? "§aAll slots marked — run §fgym-mark export"
        : "§7Skipped. Now marking §f" + next + suffix(id));
      return;
    }

    String slot = currentSlot.computeIfAbsent(id, k -> firstOpenSlot(null));
    if (slot == null) {
      actionbar(sp, "§aAll slots marked — run §fgym-mark export");
      return;
    }
    boolean isBox = "box".equals(kindOf(slot));
    GymMarkStorage.MarkEntry existing = storage.get(slot);
    boolean secondCorner = isBox && existing != null && !existing.complete;

    Pending p = pending.get(id);
    long now = sp.serverLevel().getGameTime();
    if (p != null && p.slot().equals(slot) && p.secondCorner() == secondCorner
        && p.pos().equals(pos) && now - p.gameTime() <= CONFIRM_WINDOW_TICKS) {
      // Second hit on the same spot — CONFIRM.
      pending.remove(id);
      confirm(sp, wand, slot, secondCorner, pos);
      return;
    }

    // First hit (or a re-aim): arm the pending mark + preview it.
    pending.put(id, new Pending(slot, secondCorner, pos, now));
    GymMarkCommand.previewPoint(sp.serverLevel(), pos);
    sp.playNotifySound(SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.4f);
    actionbar(sp, "§e" + slot + suffixFor(secondCorner, isBox) + " §7at §f"
      + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " §7— hit the same spot again to confirm");
  }

  private static void confirm(ServerPlayer sp, ItemStack wand, String slot, boolean secondCorner, BlockPos pos) {
    boolean isBox = "box".equals(kindOf(slot));
    if (!isBox) {
      GymMarkStorage.MarkEntry entry = new GymMarkStorage.MarkEntry();
      entry.key = slot;
      entry.kind = "point";
      entry.dimension = sp.serverLevel().dimension().location().toString();
      entry.x = pos.getX(); entry.y = pos.getY(); entry.z = pos.getZ();
      entry.complete = true;
      storage.put(entry);
      GymMarkCommand.previewPoint(sp.serverLevel(), pos);
      chat(sp, "§a[GymMark] §f" + slot + " §7confirmed at §e"
        + pos.getX() + " " + pos.getY() + " " + pos.getZ());
    } else if (secondCorner && storage.get(slot) == null) {
      // The start corner was cleared between the clicks (gym-mark clear) — fall back
      // to recording THIS confirm as corner 1 instead of NPEing on the missing entry.
      confirm(sp, wand, slot, false, pos);
      return;
    } else if (!secondCorner) {
      GymMarkStorage.MarkEntry entry = new GymMarkStorage.MarkEntry();
      entry.key = slot;
      entry.kind = "box";
      entry.dimension = sp.serverLevel().dimension().location().toString();
      entry.x = pos.getX(); entry.y = pos.getY(); entry.z = pos.getZ();
      entry.complete = false;
      storage.put(entry);
      rename(wand, sp.getUUID()); // same slot, now "(corner 2 of 2)"
      chat(sp, "§a[GymMark] §f" + slot + " §7corner 1 confirmed — walk to the far corner and double-click it");
      sp.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0f, 1.2f);
      return; // stay on this slot for corner 2
    } else {
      GymMarkStorage.MarkEntry entry = storage.get(slot);
      entry.x2 = pos.getX(); entry.y2 = pos.getY(); entry.z2 = pos.getZ();
      entry.complete = true;
      storage.put(entry);
      GymMarkCommand.previewBox(sp.serverLevel(), entry);
      int dx = Math.abs(entry.x2 - entry.x) + 1, dy = Math.abs(entry.y2 - entry.y) + 1, dz = Math.abs(entry.z2 - entry.z) + 1;
      chat(sp, "§a[GymMark] §f" + slot + " §7box confirmed (" + dx + "x" + dy + "x" + dz + ")");
    }

    sp.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.6f);
    String next = advanceTo(sp.getUUID(), firstOpenSlot(slot));
    rename(wand, sp.getUUID());
    actionbar(sp, next == null
      ? "§aThat was the last one — run §fgym-mark export"
      : "§7Now marking §f" + next + suffix(sp.getUUID()));
  }

  // ── Slot queue ───────────────────────────────────────────────────────────────

  /** The first registry slot that is not complete, searching from AFTER {@code after}
   * (wrapping) when given, else from the top. Null when everything is marked. */
  private static String firstOpenSlot(String after) {
    List<String> keys = List.copyOf(GymMarkCommand.SLOTS.keySet());
    int from = after == null ? 0 : (keys.indexOf(after) + 1) % keys.size();
    for (int i = 0; i < keys.size(); i++) {
      String key = keys.get((from + i) % keys.size());
      GymMarkStorage.MarkEntry m = storage.get(key);
      if (m == null || !m.complete) return key;
    }
    return null;
  }

  /** The next registry slot after the current one, wrapping — INCLUDING completed ones
   * (a skip-cycle lets the showrunner revisit/redo any slot). */
  private static String nextSlotAfter(String current) {
    List<String> keys = List.copyOf(GymMarkCommand.SLOTS.keySet());
    if (keys.isEmpty()) return null;
    int idx = current == null ? -1 : keys.indexOf(current);
    return keys.get((idx + 1) % keys.size());
  }

  private static String advanceTo(UUID id, String slot) {
    if (slot == null) currentSlot.remove(id); else currentSlot.put(id, slot);
    return slot;
  }

  private static String kindOf(String slot) {
    String[] def = GymMarkCommand.SLOTS.get(slot);
    return def != null ? def[0] : "point";
  }

  private static String suffix(UUID id) {
    String slot = currentSlot.get(id);
    if (slot == null || !"box".equals(kindOf(slot))) return "";
    GymMarkStorage.MarkEntry m = storage.get(slot);
    return suffixFor(m != null && !m.complete, true);
  }

  private static String suffixFor(boolean secondCorner, boolean isBox) {
    if (!isBox) return "";
    return secondCorner ? " §8(corner 2 of 2)" : " §8(corner 1 of 2)";
  }

  // ── Item plumbing ────────────────────────────────────────────────────────────

  private static boolean isWand(ItemStack stack) {
    if (stack == null || stack.isEmpty() || !stack.is(Items.AMETHYST_SHARD)) return false;
    // CUSTOM_DATA marker, not the display name: the name starts with "§b" (color code),
    // so a NAME_PREFIX startsWith test silently never matches — the review caught the
    // whole click flow dead on first use. The marker also survives anvil renames.
    net.minecraft.world.item.component.CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.copyTag().getBoolean("ci_gym_wand");
  }

  /** The wand's name IS its UI: "GymMark: <slot> (corner 1 of 2)" / "GymMark: all marked". */
  private static void rename(ItemStack wand, UUID id) {
    String slot = currentSlot.get(id);
    String label = slot == null
      ? "§b" + NAME_PREFIX + "§7: §aall marked — export!"
      : "§b" + NAME_PREFIX + "§7: §f" + slot + suffix(id);
    wand.set(DataComponents.CUSTOM_NAME, Component.literal(label));
  }

  private static void actionbar(ServerPlayer sp, String text) {
    sp.displayClientMessage(Component.literal(text), true);
  }

  private static void chat(ServerPlayer sp, String text) {
    sp.sendSystemMessage(Component.literal(text));
  }
}

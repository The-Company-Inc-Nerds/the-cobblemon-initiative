package com.thecompanyinc.cobblemoninitiative.devtools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;

/**
 * Dev-only block-MARKER tool ({@code /ca-dev marker}). Hold the Marker Tool, then:
 * <ul>
 *   <li><b>Left-click a block</b> → set the anchor (a pending position; does not break the block).</li>
 *   <li><b>Right-click a block</b> → add to the selection: the SAME block as the anchor confirms a
 *       single block; a DIFFERENT block fills the whole BOX between them. Right-click with no anchor
 *       quick-adds that one block. Repeat to build any shape.</li>
 * </ul>
 * Selected blocks are highlighted in-world (green dust; the pending anchor is red).
 * {@code /ca-dev marker save <title> [description]} saves the accumulated block array to the shared
 * dev journal (see {@link DevNoteInit}) and resets; {@code clear} / {@code undo} manage the take.
 * Registered by {@link DevToolsInit}; strips with the devtools package at 1.0.0.
 */
public final class DevMarkerManager {

  private DevMarkerManager() {}

  public static final String TOOL_NAME = "Marker Tool";
  /** Guard on a huge accidental box (fly-to-sky double-click); the file/array can hold plenty. */
  private static final int MAX_BOX = 50000;
  /** Cap on per-tick highlight particles per player (the saved array is unlimited). */
  private static final int HIGHLIGHT_CAP = 1500;

  /** One player's in-progress selection (in memory until saved). */
  private static final class Sel {
    BlockPos anchor;                                   // pending left-click position, or null
    final LinkedHashSet<BlockPos> blocks = new LinkedHashSet<>();
    final Deque<List<BlockPos>> undo = new ArrayDeque<>();   // per-add block lists, for undo
  }

  private static final Map<UUID, Sel> selections = new HashMap<>();
  private static boolean initialized;

  public static void register() {
    if (initialized) return;
    initialized = true;

    // Left-click a block with the tool → set the anchor (never break the block).
    AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
      if (world.isClientSide() || hand != InteractionHand.MAIN_HAND || !holding(player)) {
        return InteractionResult.PASS;
      }
      if (player instanceof ServerPlayer sp) setAnchor(sp, pos);
      return InteractionResult.FAIL; // consume — do not start breaking
    });

    // Right-click a block with the tool → confirm single / fill box / quick-add.
    UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
      if (world.isClientSide() || hand != InteractionHand.MAIN_HAND || !holding(player)) {
        return InteractionResult.PASS;
      }
      if (player instanceof ServerPlayer sp) {
        addAt(sp, ((BlockHitResult) hit).getBlockPos());
        return InteractionResult.SUCCESS;
      }
      return InteractionResult.PASS;
    });

    // Highlight the live selection (green) + the pending anchor (red) for tool holders.
    ServerTickEvents.END_SERVER_TICK.register(DevMarkerManager::highlightTick);

    // Drop all in-progress selections on server stop — this static map lives in the mod
    // classloader and would otherwise resurrect world A's coords in world B (the DevWandTool
    // / AutoBattler guard: never let one world's staged state bleed into another's).
    ServerLifecycleEvents.SERVER_STOPPING.register(server -> selections.clear());
  }

  // ── item ─────────────────────────────────────────────────────────────────────────

  public static ItemStack makeTool() {
    ItemStack rod = new ItemStack(Items.BLAZE_ROD);
    rod.set(DataComponents.CUSTOM_NAME, Component.literal("§6" + TOOL_NAME));
    return rod;
  }

  private static boolean holding(Player player) {
    ItemStack held = player.getMainHandItem();
    Component name = held.get(DataComponents.CUSTOM_NAME);
    return held.is(Items.BLAZE_ROD) && name != null && name.getString().contains(TOOL_NAME);
  }

  public static void giveTool(ServerPlayer player) {
    player.getInventory().add(makeTool());
    player.sendSystemMessage(Component.literal(
      "§6Marker Tool §7given. §fLeft-click§7 a block to set the anchor, §fright-click§7 another to add "
      + "the box (same block = single). §f/ca-dev marker save <title> [desc]§7 to save, §fclear§7/§fundo§7."));
  }

  // ── selection ──────────────────────────────────────────────────────────────────

  private static Sel sel(ServerPlayer p) {
    return selections.computeIfAbsent(p.getUUID(), k -> new Sel());
  }

  private static void setAnchor(ServerPlayer p, BlockPos pos) {
    Sel s = sel(p);
    s.anchor = pos.immutable();
    p.displayClientMessage(Component.literal(
      "§6Marker §7anchor at §f" + pos.getX() + " " + pos.getY() + " " + pos.getZ()
      + " §7— right-click a block to add (same = single, other = box)."), true);
  }

  /** Right-click add: box anchor..pos (single if same/none), deduped into the selection. */
  private static void addAt(ServerPlayer p, BlockPos pos) {
    Sel s = sel(p);
    BlockPos a = s.anchor != null ? s.anchor : pos;
    long volume = (long) (Math.abs(a.getX() - pos.getX()) + 1)
      * (Math.abs(a.getY() - pos.getY()) + 1) * (Math.abs(a.getZ() - pos.getZ()) + 1);
    if (volume > MAX_BOX) {
      p.displayClientMessage(Component.literal(
        "§cThat box is " + volume + " blocks (>" + MAX_BOX + "). Anchor cleared — pick a smaller region."), true);
      s.anchor = null;
      return;
    }
    List<BlockPos> added = new ArrayList<>();
    int x1 = Math.min(a.getX(), pos.getX()), x2 = Math.max(a.getX(), pos.getX());
    int y1 = Math.min(a.getY(), pos.getY()), y2 = Math.max(a.getY(), pos.getY());
    int z1 = Math.min(a.getZ(), pos.getZ()), z2 = Math.max(a.getZ(), pos.getZ());
    for (int x = x1; x <= x2; x++) {
      for (int y = y1; y <= y2; y++) {
        for (int z = z1; z <= z2; z++) {
          BlockPos bp = new BlockPos(x, y, z);
          if (s.blocks.add(bp)) added.add(bp);
        }
      }
    }
    s.anchor = null;
    if (!added.isEmpty()) s.undo.push(added);
    p.displayClientMessage(Component.literal(
      "§6Marker §a+" + added.size() + " §7block(s) §8(total " + s.blocks.size() + ")"), true);
  }

  public static void clear(ServerPlayer p) {
    Sel s = selections.remove(p.getUUID());
    int n = s == null ? 0 : s.blocks.size();
    p.sendSystemMessage(Component.literal("§6Marker §7selection cleared §8(" + n + " block(s))."));
  }

  public static void undo(ServerPlayer p) {
    Sel s = selections.get(p.getUUID());
    if (s == null || s.undo.isEmpty()) {
      p.sendSystemMessage(Component.literal("§6Marker §7nothing to undo."));
      return;
    }
    List<BlockPos> last = s.undo.pop();
    last.forEach(s.blocks::remove);
    p.sendSystemMessage(Component.literal(
      "§6Marker §7undid last add §8(-" + last.size() + ", total " + s.blocks.size() + ")."));
  }

  public static void status(ServerPlayer p) {
    Sel s = selections.get(p.getUUID());
    int n = s == null ? 0 : s.blocks.size();
    p.sendSystemMessage(Component.literal(
      "§6Marker §7selection: §f" + n + " §7block(s)" + (s != null && s.anchor != null
        ? " §7(anchor at §f" + s.anchor.getX() + " " + s.anchor.getY() + " " + s.anchor.getZ() + "§7)" : "")));
  }

  /** Save the current selection to the dev journal as a titled marker, then reset. */
  public static boolean save(ServerPlayer p, String title, String description) {
    Sel s = selections.get(p.getUUID());
    if (s == null || s.blocks.isEmpty()) {
      p.sendSystemMessage(Component.literal("§cMarker §7selection is empty — mark some blocks first."));
      return false;
    }
    DevNoteStorage store = DevNoteInit.getStorage();
    if (store == null) {
      p.sendSystemMessage(Component.literal("§cMarker storage not ready (server not started?)."));
      return false;
    }
    DevNoteStorage.Marker m = new DevNoteStorage.Marker();
    m.title = title;
    m.description = (description != null && !description.isBlank()) ? description : null;
    m.stamp = java.time.LocalDateTime.now()
      .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    m.dim = p.level().dimension().location().toString();
    for (BlockPos b : s.blocks) m.blocks.add(new int[] {b.getX(), b.getY(), b.getZ()});
    store.getMarkers().add(m);
    store.save();
    int n = m.blocks.size();
    selections.remove(p.getUUID());
    p.sendSystemMessage(Component.literal(
      "§6Marker §asaved §f\"" + title + "\" §7(" + n + " block(s))"
      + (m.description != null ? " §8— " + m.description : "") + "§7. Selection reset. §f/ca-dev log§7 to export."));
    return true;
  }

  // ── highlight ────────────────────────────────────────────────────────────────────

  private static final DustParticleOptions GREEN =
    new DustParticleOptions(new Vector3f(0.3f, 1.0f, 0.3f), 1.0f);
  private static final DustParticleOptions RED =
    new DustParticleOptions(new Vector3f(1.0f, 0.2f, 0.2f), 1.3f);

  private static void highlightTick(net.minecraft.server.MinecraftServer server) {
    if (selections.isEmpty() || server.getTickCount() % 10 != 0) return;
    for (Map.Entry<UUID, Sel> e : selections.entrySet()) {
      ServerPlayer p = server.getPlayerList().getPlayer(e.getKey());
      if (p == null || !holding(p) || !(p.level() instanceof ServerLevel level)) continue;
      Sel s = e.getValue();
      int drawn = 0;
      for (BlockPos b : s.blocks) {
        if (drawn++ >= HIGHLIGHT_CAP) break;
        level.sendParticles(GREEN, b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
      }
      if (s.anchor != null) {
        level.sendParticles(RED, s.anchor.getX() + 0.5, s.anchor.getY() + 0.5, s.anchor.getZ() + 0.5,
          2, 0.05, 0.05, 0.05, 0.0);
      }
    }
  }
}

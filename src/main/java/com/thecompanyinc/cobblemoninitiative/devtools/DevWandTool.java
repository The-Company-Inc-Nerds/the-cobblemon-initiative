package com.thecompanyinc.cobblemoninitiative.devtools;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.BlockHitResult;

/**
 * THE PRODUCER'S TOOL — the one-item field workflow for every marking walk (strip with
 * the devtools package at 1.0.0). {@code /cobblemon-initiative dev tool} to get it.
 *
 * <p>It walks a single queue = the 51 placement-plan entries + the 33 gym-mark slots,
 * writing into the SAME stores as the typed commands (npc_placements.json /
 * gym_marks.json), so `dev place`, `gym-mark`, and both exports stay valid fallbacks.
 *
 * <p>Interactions (per the showrunner spec, 2026-07-11):
 * <ul>
 *   <li><b>Holding it</b> — flight + invulnerability. Switching away removes both and
 *       nothing else (airborne grace: kept until you touch ground — a hardcore save
 *       must never die to a hotbar scroll at altitude).</li>
 *   <li><b>Left click</b> — set the PRIMARY position: a block = stand-on spot (the block
 *       face you hit, +1 for placements; the block itself for gym marks); an NPC = stage
 *       a body ADOPTION (placement entries).</li>
 *   <li><b>Right click</b> — set the SECONDARY position (box slots only; air = your feet
 *       for floating corners).</li>
 *   <li><b>F (swap-hands)</b> — set primary = your own feet (middle-click was the spec
 *       but creative pick-block never reaches the server; F fires unconditionally).</li>
 *   <li><b>Q (drop)</b> — skip the current item and move on (the item never drops).</li>
 *   <li><b>Chat after a set</b> — every chat line is captured as a note on the current
 *       item (not broadcast) until the walk advances.</li>
 *   <li><b>Glint</b> — the tool glows while the CURRENT item already has anything
 *       recorded or staged, so cycling around the list shows what is done.</li>
 *   <li><b>Shift+left / shift+right</b> — CONFIRM primary / secondary. The walk advances
 *       when everything the item needs is confirmed this visit (primary only for
 *       single-position items, both for boxes) — or on Q.</li>
 * </ul>
 */
public final class DevWandTool {

  private static final String MARKER = "ci_dev_tool";
  private static final String NAME = "§6Producer's Tool";

  private static GymMarkStorage gymMarks;

  /** One walk stop: a placement-plan entry or a gym-mark slot. */
  private record QueueItem(String key, boolean gym, String id, boolean box) {}

  // Single-player campaign tool — session state is static like the sibling tools.
  private static List<QueueItem> queue;
  private static MinecraftServer queueFrom;
  private static int cursor = -1;

  /** Adoption staging is a snapshot, never an Entity reference — a live reference would
   *  pin the whole unloaded world in memory across a quit-to-title. */
  private record AdoptionSnapshot(java.util.UUID uuid, String label, BlockPos pos) {}

  // Per-visit staging (cleared on every advance and on server stop).
  private static BlockPos stagedPrimary;
  private static BlockPos stagedSecondary;
  private static AdoptionSnapshot stagedAdoption;
  private static boolean confirmedPrimary;
  private static boolean confirmedSecondary;
  private static boolean notesArmed;

  /**
   * Grant tracking rides a persistent player TAG (not session state): a relog or server
   * restart while holding the tool must never strand a hardcore player permanently
   * invulnerable/flying with no revoke path.
   */
  private static final String GRANT_TAG = "ci_tool_flight";

  private DevWandTool() {}

  // ---------------------------------------------------------------------------
  // Wiring
  // ---------------------------------------------------------------------------

  public static void registerEvents(GymMarkStorage marks) {
    gymMarks = marks;

    // World switch = new queue and a cold cursor: never let world A's staged coords or
    // confirm flags write into world B's files.
    net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
      queue = null;
      queueFrom = null;
      cursor = -1;
      clearVisit();
    });

    // LEFT CLICK block: set primary — sneak confirms instead. The client consumes too
    // (isTool is component-readable both sides): no dig prediction, no follow-up packet.
    AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
      if (hand != InteractionHand.MAIN_HAND || !isTool(player.getMainHandItem())) return InteractionResult.PASS;
      if (world.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
      if (player.isShiftKeyDown()) confirmPrimary(sp);
      else setPrimary(sp, pos, direction);
      return InteractionResult.SUCCESS; // never actually dig
    });

    // LEFT CLICK entity: stage adoption (placement entries) — sneak still confirms.
    AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
      if (hand != InteractionHand.MAIN_HAND || !isTool(player.getMainHandItem())) return InteractionResult.PASS;
      if (world.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
      if (player.isShiftKeyDown()) confirmPrimary(sp);
      else stageAdoption(sp, entity);
      return InteractionResult.SUCCESS; // never damage the NPC
    });

    // RIGHT CLICK block: set secondary — sneak confirms secondary. Client consumes so
    // startUseItem never falls through to a second UseItem packet (which would have
    // overwritten the staged corner with the player's feet).
    UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
      if (hand != InteractionHand.MAIN_HAND || !isTool(player.getItemInHand(hand))) return InteractionResult.PASS;
      if (world.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
      if (player.isShiftKeyDown()) confirmSecondary(sp);
      else setSecondary(sp, ((BlockHitResult) hitResult).getBlockPos());
      return InteractionResult.SUCCESS;
    });

    // RIGHT CLICK air: secondary = own feet (floating box corners) — sneak confirms.
    UseItemCallback.EVENT.register((player, world, hand) -> {
      ItemStack held = player.getItemInHand(hand);
      if (hand != InteractionHand.MAIN_HAND || !isTool(held)) {
        return InteractionResultHolder.pass(held);
      }
      if (!world.isClientSide && player instanceof ServerPlayer sp) {
        if (player.isShiftKeyDown()) confirmSecondary(sp);
        else setSecondary(sp, sp.blockPosition());
      }
      return InteractionResultHolder.success(held);
    });

    // CHAT while a position is staged: capture as a note on the current item.
    ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
      if (!notesArmed || !isTool(sender.getMainHandItem())) return true;
      QueueItem item = current(sender.getServer());
      if (item == null) return true;
      String note = message.signedContent();
      DevPlaceManager.addNote(sender.getServer(), item.key(), note);
      sender.sendSystemMessage(Component.literal("§8✎ noted on §7" + item.key() + "§8: §f" + note));
      return false; // captured, not broadcast
    });

    // Flight + invulnerability while held; graceful revoke on the ground. The GRANT_TAG
    // persists across relog/restart, so a stranded grant always finds its revoke path.
    ServerTickEvents.END_SERVER_TICK.register(server -> {
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
        boolean holding = isTool(p.getMainHandItem());
        if (holding) {
          // abilities.invulnerable (NOT the entity Invulnerable NBT flag): vanilla's
          // loadGameTypes resets abilities per game type on every join, so even a jar
          // without this mod self-heals a live grant — no permanent hardcore god-mode.
          if (!p.getAbilities().mayfly || !p.getAbilities().invulnerable) {
            p.getAbilities().mayfly = true;
            p.getAbilities().invulnerable = true;
            p.onUpdateAbilities();
          }
          p.addTag(GRANT_TAG);
          updateGlint(p); // keeps the glow honest after typed-command resolutions too
        } else if (p.getTags().contains(GRANT_TAG)) {
          // Grace: only strip flight once they're standing — never drop a hardcore
          // player out of the sky because they scrolled off the tool.
          if (p.onGround() || p.isCreative()) {
            if (!p.isCreative()) {
              p.getAbilities().mayfly = false;
              p.getAbilities().flying = false;
              p.getAbilities().invulnerable = false;
              p.onUpdateAbilities();
            }
            p.removeTag(GRANT_TAG);
          }
        }
      }
    });
  }

  // ---------------------------------------------------------------------------
  // Mixin hooks (no Fabric events exist for these)
  // ---------------------------------------------------------------------------

  /** F (swap-hands): primary = the player's own feet. Middle click was the spec, but
   *  creative pick-block never reaches the server (bytecode-verified) — F always does. */
  public static boolean handleFeetKey(ServerPlayer player) {
    if (!isTool(player.getMainHandItem())) return false;
    setPrimary(player, player.blockPosition(), null);
    return true;
  }

  /** Q / drop: skip the current item and advance; the tool never actually drops. */
  public static boolean handleDropKey(ServerPlayer player) {
    if (!isTool(player.getMainHandItem())) return false;
    MinecraftServer server = player.getServer();
    QueueItem item = current(server);
    if (item != null) {
      if (!item.gym() && !DevPlaceManager.isResolved(server, item.id())) {
        DevPlaceManager.recordSkip(server, item.id());
      }
      player.sendSystemMessage(Component.literal("§7✖ skipped §f" + item.key()));
    }
    advance(player);
    return true;
  }

  // ---------------------------------------------------------------------------
  // Set / confirm
  // ---------------------------------------------------------------------------

  private static void setPrimary(ServerPlayer player, BlockPos clicked, net.minecraft.core.Direction face) {
    QueueItem item = current(player.getServer());
    if (item == null) { hintNoItem(player); return; }
    // Placements record a STANDING spot: the block adjacent to the clicked FACE (floor
    // click = stand on it, wall click = stand beside it). face == null means "my feet".
    stagedPrimary = (!item.gym() && face != null) ? clicked.relative(face) : clicked;
    stagedAdoption = null;
    confirmedPrimary = false; // a re-staged position is never pre-confirmed
    afterSet(player, item, "primary §f" + shortPos(stagedPrimary)
      + (item.box() ? " §7(right-click sets the far corner)" : ""));
  }

  private static void stageAdoption(ServerPlayer player, Entity entity) {
    QueueItem item = current(player.getServer());
    if (item == null) { hintNoItem(player); return; }
    if (item.gym()) {
      player.sendSystemMessage(Component.literal("§cGym slots take block positions — left-click a block."));
      return;
    }
    if (entity instanceof Player) return;
    stagedAdoption = new AdoptionSnapshot(entity.getUUID(), entity.getName().getString(), entity.blockPosition());
    stagedPrimary = null;
    confirmedPrimary = false;
    afterSet(player, item, "adopt §f" + stagedAdoption.label() + " §7(" + stagedAdoption.uuid() + ")");
  }

  private static void setSecondary(ServerPlayer player, BlockPos pos) {
    QueueItem item = current(player.getServer());
    if (item == null) { hintNoItem(player); return; }
    if (!item.box()) {
      player.sendSystemMessage(Component.literal(
        "§7No secondary needed for §f" + item.key() + "§7 — shift+left-click to confirm the primary."));
      return;
    }
    stagedSecondary = pos;
    confirmedSecondary = false; // a re-staged corner is never pre-confirmed
    afterSet(player, item, "secondary §f" + shortPos(pos));
  }

  private static void confirmPrimary(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    QueueItem item = current(server);
    if (item == null) { hintNoItem(player); return; }
    if (stagedPrimary == null && stagedAdoption == null) {
      player.sendSystemMessage(Component.literal("§cNothing staged — left-click a spot (or NPC) first."));
      return;
    }
    if (item.gym()) {
      if (item.box()) {
        confirmedPrimary = true;
        player.sendSystemMessage(Component.literal("§a✔ corner 1 confirmed — " +
          (confirmedSecondary ? "writing the box." : "set + shift+right-click the far corner.")));
        maybeFinishBox(player, item);
      } else {
        GymMarkStorage.MarkEntry e = new GymMarkStorage.MarkEntry();
        e.key = item.id();
        e.kind = "point";
        e.dimension = player.serverLevel().dimension().location().toString();
        e.x = stagedPrimary.getX(); e.y = stagedPrimary.getY(); e.z = stagedPrimary.getZ();
        e.complete = true;
        gymMarks.put(e);
        done(player, item, "§a✔ §e" + item.id() + "§a marked at §f" + shortPos(stagedPrimary));
      }
      return;
    }
    // Placement entry: adoption beats position when both were staged.
    if (stagedAdoption != null) {
      DevPlaceManager.recordAdoption(server, item.id(), stagedAdoption.uuid(), stagedAdoption.label(), stagedAdoption.pos());
      done(player, item, "§b⚑ §e" + item.id() + "§b takes over §f" + stagedAdoption.label());
    } else {
      DevPlaceManager.recordPlacement(server, item.id(),
        stagedPrimary.getX(), stagedPrimary.getY(), stagedPrimary.getZ());
      done(player, item, "§a✔ §e" + item.id() + "§a placed at §f" + shortPos(stagedPrimary));
    }
  }

  private static void confirmSecondary(ServerPlayer player) {
    QueueItem item = current(player.getServer());
    if (item == null) { hintNoItem(player); return; }
    if (!item.box()) {
      confirmPrimary(player); // single-position items: any confirm confirms THE position
      return;
    }
    if (stagedSecondary == null) {
      player.sendSystemMessage(Component.literal("§cNo secondary staged — right-click the far corner first."));
      return;
    }
    confirmedSecondary = true;
    player.sendSystemMessage(Component.literal("§a✔ corner 2 confirmed — " +
      (confirmedPrimary ? "writing the box." : "set + shift+left-click corner 1.")));
    maybeFinishBox(player, item);
  }

  /** Both corners newly confirmed → write the gym box and advance. */
  private static void maybeFinishBox(ServerPlayer player, QueueItem item) {
    if (!confirmedPrimary || !confirmedSecondary) return;
    GymMarkStorage.MarkEntry e = new GymMarkStorage.MarkEntry();
    e.key = item.id();
    e.kind = "box";
    e.dimension = player.serverLevel().dimension().location().toString();
    e.x = stagedPrimary.getX(); e.y = stagedPrimary.getY(); e.z = stagedPrimary.getZ();
    e.x2 = stagedSecondary.getX(); e.y2 = stagedSecondary.getY(); e.z2 = stagedSecondary.getZ();
    e.complete = true;
    gymMarks.put(e);
    done(player, item, "§a✔ §e" + item.id() + "§a box " + shortPos(stagedPrimary) + " → " + shortPos(stagedSecondary));
  }

  // ---------------------------------------------------------------------------
  // Walk mechanics
  // ---------------------------------------------------------------------------

  /** Give the tool (or just resume) and brief the current stop. */
  public static int cmdTool(ServerPlayer player) {
    boolean hasOne = false;
    for (ItemStack st : player.getInventory().items) {
      if (isTool(st)) {
        hasOne = true;
        st.set(DataComponents.LORE, controlsLore()); // refresh older copies' tooltip
      }
    }
    if (!hasOne) player.getInventory().add(make());
    player.sendSystemMessage(Component.literal(
      "§6Producer's Tool§7: hold = fly + invulnerable · left = set · right = 2nd corner ·"
        + " F = my feet · shift+click = confirm · Q = skip · chat after a set = note."));
    if (cursor < 0) advance(player);
    else brief(player, current(player.getServer()));
    return 1;
  }

  private static void advance(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    List<QueueItem> q = queue(server);
    if (q.isEmpty()) {
      player.sendSystemMessage(Component.literal("§cNo walk data bundled (placement plan + gym slots both empty)."));
      return;
    }
    clearVisit();
    for (int i = 1; i <= q.size(); i++) {
      int idx = Math.floorMod(cursor + i, q.size());
      if (!resolved(server, q.get(idx))) {
        cursor = idx;
        QueueItem item = q.get(idx);
        if (!item.gym()) {
          JsonObject entry = DevPlaceManager.entryById(server, item.id());
          if (entry != null) DevPlaceManager.visitEntry(player, entry); // tp + full brief
        } else {
          brief(player, item);
        }
        updateGlint(player);
        return;
      }
    }
    player.sendSystemMessage(Component.literal(
      "§aThe whole walk is resolved — `dev place export` + `gym-mark export` to hand off."));
  }

  private static void brief(ServerPlayer player, QueueItem item) {
    if (item == null) return;
    if (item.gym()) {
      String[] slot = GymMarkCommand.SLOTS.get(item.id());
      String desc = slot != null ? slot[1] : item.id();
      player.sendSystemMessage(Component.literal("§6=== GymMark: " + item.id() + " §7(" + (item.box() ? "box — two corners" : "point") + ") §6==="));
      player.sendSystemMessage(Component.literal("§7" + desc));
      player.sendSystemMessage(Component.literal(item.box()
        ? "§8left = corner 1 · right = corner 2 · shift+left/right = confirm each · Q = skip"
        : "§8left = mark block · F = my feet · shift+left = confirm · Q = skip"));
    } else {
      JsonObject entry = DevPlaceManager.entryById(player.getServer(), item.id());
      if (entry != null) DevPlaceManager.visitEntry(player, entry);
    }
  }

  private static void afterSet(ServerPlayer player, QueueItem item, String what) {
    notesArmed = true;
    updateGlint(player);
    player.playNotifySound(SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 0.8f, 1.4f);
    player.sendSystemMessage(Component.literal("§7staged " + what
      + " §8— shift+click confirms · chat lines become notes on §7" + item.key()));
  }

  private static void done(ServerPlayer player, QueueItem item, String message) {
    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.5f);
    player.sendSystemMessage(Component.literal(message));
    advance(player);
  }

  private static void hintNoItem(ServerPlayer player) {
    player.sendSystemMessage(Component.literal("§cNo current walk stop — run `/ca dev tool` to start."));
  }

  private static String shortPos(BlockPos pos) {
    return pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  private static void clearVisit() {
    stagedPrimary = null;
    stagedSecondary = null;
    stagedAdoption = null;
    confirmedPrimary = false;
    confirmedSecondary = false;
    notesArmed = false;
  }

  private static QueueItem current(MinecraftServer server) {
    List<QueueItem> q = queue(server);
    if (cursor < 0 || cursor >= q.size()) return null;
    return q.get(cursor);
  }

  private static boolean resolved(MinecraftServer server, QueueItem item) {
    if (item.gym()) {
      GymMarkStorage.MarkEntry m = gymMarks.get(item.id());
      return m != null && m.complete; // a half-marked box still needs its stop corner
    }
    return DevPlaceManager.isResolved(server, item.id());
  }

  /** Glint = the CURRENT stop already has something recorded or staged. */
  private static void updateGlint(ServerPlayer player) {
    ItemStack held = player.getMainHandItem();
    if (!isTool(held)) return;
    QueueItem item = current(player.getServer());
    boolean glow = item != null && (resolved(player.getServer(), item)
      || stagedPrimary != null || stagedSecondary != null || stagedAdoption != null);
    held.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glow);
  }

  private static List<QueueItem> queue(MinecraftServer server) {
    if (queue != null && queueFrom == server) return queue;
    List<QueueItem> q = new ArrayList<>();
    for (JsonObject e : DevPlaceManager.planEntries(server)) {
      String id = e.get("id").getAsString();
      q.add(new QueueItem(id, false, id, false));
    }
    for (Map.Entry<String, String[]> slot : GymMarkCommand.SLOTS.entrySet()) {
      q.add(new QueueItem("gym:" + slot.getKey(), true, slot.getKey(), "box".equals(slot.getValue()[0])));
    }
    queue = q;
    queueFrom = server;
    return q;
  }

  // ---------------------------------------------------------------------------
  // Item identity
  // ---------------------------------------------------------------------------

  private static ItemStack make() {
    ItemStack stack = new ItemStack(Items.RECOVERY_COMPASS);
    stack.set(DataComponents.CUSTOM_NAME, Component.literal(NAME));
    stack.set(DataComponents.LORE, controlsLore());
    CompoundTag tag = new CompoundTag();
    tag.putBoolean(MARKER, true);
    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    return stack;
  }

  /** The control scheme as the item tooltip — the walk's cheat-sheet lives on the tool. */
  private static net.minecraft.world.item.component.ItemLore controlsLore() {
    List<Component> lines = new ArrayList<>();
    for (String line : new String[] {
      "Hold — fly + invulnerable",
      "Left click — set spot (on an NPC = adopt)",
      "Right click — far corner (air = feet)",
      "F — my feet here · Q — skip stop",
      "Sneak + L/R click — confirm",
      "Chat after a set — note on this stop",
      "Glint — this stop already has data",
    }) {
      lines.add(Component.literal(line).withStyle(style ->
        style.withColor(net.minecraft.ChatFormatting.GRAY).withItalic(false)));
    }
    return new net.minecraft.world.item.component.ItemLore(lines);
  }

  static boolean isTool(ItemStack stack) {
    if (!stack.is(Items.RECOVERY_COMPASS)) return false;
    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
    return data != null && data.contains(MARKER); // no copyTag — this runs every tick
  }
}

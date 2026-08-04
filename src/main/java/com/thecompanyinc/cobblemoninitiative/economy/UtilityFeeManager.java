package com.thecompanyinc.cobblemoninitiative.economy;

import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Town workstations belong to the TOWNSFOLK — using somebody's furnace, crafting table, or
 * enchanting table means paying the OWNER for the privilege (showrunner 2026-08-04: it is NOT
 * the Company metering these; the receipt goes to the craftsperson whose gear it is). Prices
 * vary by station: everyday tools (crafting table, furnace) are cheap; specialist gear (anvils,
 * brewing) costs real money; an enchanting table is the town's crown jewel and priced like it.
 * The pressure to build your own powered base still lands early — the cheap tiers are courtesy,
 * the expensive ones are the point. Storage and passage blocks (chests, barrels, doors, buttons,
 * beds, …) stay free.
 *
 * <p>Playtest 2026-08-03 note 1: the fee is now HIGH and the station no longer opens on the first
 * click — the click is cancelled and a chat receipt asks for explicit consent ("[Pay N CD]" click →
 * {@code /cobblemon-initiative utility confirm <pos>}). The confirm re-validates (distance, block,
 * town), probes affordability with the CobbleDollars {@code pay @s} self-pay (net-zero; result 0 =
 * broke — the same probe {@code economy/market/charge} uses, since there is no Java CobbleDollars
 * API), deducts, stamps the per-block cooldown, and opens the workstation menu programmatically.
 * Within the cooldown window re-clicks open normally without a prompt (already paid up).
 *
 * <p>Only fires inside a {@code TOWN} safe zone; creative players are exempt.
 */
public final class UtilityFeeManager {

  private UtilityFeeManager() {}

  /** Owner's rate per station type (showrunner 2026-08-04: varying prices — everyday tools
   *  cheap, the enchanting table "quite expensive"). Values are the 100%-multiplier bases;
   *  everything rides the ModMenu Economy multiplier ({@link #scaledFee}). Deliberately does
   *  NOT scale with badges — that role belongs to the Center heal fee (economy/heal_paid,
   *  200 + 100×badges + 2×instability). Storage/passage/redstone blocks are absent = free. */
  private static final Map<Block, Integer> FEE_BY_BLOCK = Map.ofEntries(
    Map.entry(Blocks.CRAFTING_TABLE, 100),
    Map.entry(Blocks.FURNACE, 150),
    Map.entry(Blocks.BLAST_FURNACE, 150),
    Map.entry(Blocks.SMOKER, 150),
    Map.entry(Blocks.GRINDSTONE, 200),
    Map.entry(Blocks.STONECUTTER, 200),
    Map.entry(Blocks.LOOM, 200),
    Map.entry(Blocks.CARTOGRAPHY_TABLE, 200),
    Map.entry(Blocks.ANVIL, 600),
    Map.entry(Blocks.CHIPPED_ANVIL, 600),
    Map.entry(Blocks.DAMAGED_ANVIL, 600),
    Map.entry(Blocks.SMITHING_TABLE, 600),
    Map.entry(Blocks.BREWING_STAND, 900),
    Map.entry(Blocks.ENCHANTING_TABLE, 2500)
  );
  /** After a confirmed payment, re-opening the same station within this window is free. */
  private static final long COOLDOWN_TICKS = 1200L;
  /** The confirm click must still be near the station (blocks). */
  private static final double CONFIRM_RANGE = 6.0;

  /** key = playerUUID + ":" + blockPos.asLong() -> game time of the last CONFIRMED payment. */
  private static final Map<String, Long> LAST_CHARGED = new HashMap<>();

  public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
    if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
    if (sp.getAbilities().instabuild) return InteractionResult.PASS; // creative: free
    NuzlockeConfig cfg = NuzlockeInit.getConfig();
    if (cfg == null) return InteractionResult.PASS;

    BlockPos pos = hit.getBlockPos();
    BlockState state = level.getBlockState(pos);
    if (!FEE_BY_BLOCK.containsKey(state.getBlock())) return InteractionResult.PASS;

    // Sneaking with an item = the player is placing/using that item on the block, not opening it.
    if (sp.isSecondaryUseActive() && !sp.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;

    MinecraftServer server = sp.getServer();
    String town = cfg.townZoneNameAt(
      level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ(), server);
    if (town == null) return InteractionResult.PASS; // your own base outside town: free

    long now = sp.level().getGameTime();
    Long last = LAST_CHARGED.get(key(sp, pos));
    if (last != null && now - last < COOLDOWN_TICKS) return InteractionResult.PASS; // still paid up

    // Not paid — cancel the open and ask. The [Pay] click routes back through utility confirm.
    int fee = scaledFee(state.getBlock());
    String cmd = "/cobblemon-initiative utility confirm "
      + pos.getX() + " " + pos.getY() + " " + pos.getZ();
    Component pay = Component.literal("§a§l[Pay " + fee + " CD]")
      .withStyle(s -> s
        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd))
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
          Component.literal("§7Pay the owner " + fee + " CD and open the "
            + state.getBlock().getName().getString()))));
    sp.sendSystemMessage(Component.literal("§6This ")
      .append(state.getBlock().getName().copy().withStyle(s -> s.withColor(0xFFE9A13B)))
      .append(Component.literal("§6 belongs to one of the townsfolk — §e" + fee + " CD§6 for the use of it. "))
      .append(pay)
      .append(Component.literal(" §8or walk away. §7Build your own and pay nobody.")));
    return InteractionResult.FAIL;
  }

  /** Confirm clicks queued for the next server tick. NESTED-CONTEXT TRAP (review-found, a18):
   *  the confirm runs inside the chat-command's brigadier executor, and in 1.20.3+ a nested
   *  performPrefixedCommand only QUEUES into the outer ExecutionContext — the withCallback probe
   *  would fire AFTER we read it (same trap NpcSightManager.runVerifiedCommand documents). From a
   *  plain END_SERVER_TICK path the dispatch is synchronous and the probe result is valid. */
  private static final java.util.List<PendingConfirm> PENDING_CONFIRMS =
    java.util.Collections.synchronizedList(new java.util.ArrayList<>());

  private record PendingConfirm(java.util.UUID player, BlockPos pos) {}

  /**
   * The chat-click confirm — perm-0 {@code /cobblemon-initiative utility confirm <pos>}.
   * Only validates + enqueues; the real probe/charge/open happens in {@link #tick} one tick
   * later, outside the nested command execution context (see PENDING_CONFIRMS note).
   */
  public static int confirmAndOpen(ServerPlayer sp, BlockPos pos) {
    Level level = sp.level();
    BlockState state = level.getBlockState(pos);
    if (!FEE_BY_BLOCK.containsKey(state.getBlock())) {
      sp.displayClientMessage(Component.literal("§7The equipment is gone."), true);
      return 0;
    }
    if (pos.getCenter().distanceTo(sp.position()) > CONFIRM_RANGE + 2.0) {
      sp.displayClientMessage(Component.literal("§7Too far from the equipment — walk back to it."), true);
      return 0;
    }
    PENDING_CONFIRMS.add(new PendingConfirm(sp.getUUID(), pos.immutable()));
    return 1;
  }

  /** Registered on END_SERVER_TICK from InitiativeInit — drains queued confirms on a plain tick
   *  path where {@code performPrefixedCommand} runs synchronously and the pay-probe result is
   *  readable before we branch. Also re-asserts the ModMenu fee multiplier into the
   *  {@code #cfg_fee_mult cd_const} scoreboard holder every ~10s (the config→scoreboard bridge —
   *  {@code economy/heal_paid} reads it, so a ModMenu change reaches the datapack without a relog). */
  public static void tick(MinecraftServer server) {
    if (server.getTickCount() % 200 == 0) assertFeeMultiplier(server);
    if (PENDING_CONFIRMS.isEmpty()) return;
    java.util.List<PendingConfirm> batch;
    synchronized (PENDING_CONFIRMS) {
      batch = new java.util.ArrayList<>(PENDING_CONFIRMS);
      PENDING_CONFIRMS.clear();
    }
    for (PendingConfirm pc : batch) {
      ServerPlayer sp = server.getPlayerList().getPlayer(pc.player());
      if (sp == null) continue;
      runConfirm(server, sp, pc.pos());
    }
  }

  private static void runConfirm(MinecraftServer server, ServerPlayer sp, BlockPos pos) {
    NuzlockeConfig cfg = NuzlockeInit.getConfig();
    Level level = sp.level();
    BlockState state = level.getBlockState(pos);
    if (!FEE_BY_BLOCK.containsKey(state.getBlock())) return;
    if (pos.getCenter().distanceTo(sp.position()) > CONFIRM_RANGE + 4.0) return;

    long now = level.getGameTime();
    Long last = LAST_CHARGED.get(key(sp, pos));
    if (last == null || now - last >= COOLDOWN_TICKS) {
      int fee = scaledFee(state.getBlock());
      // Affordability probe: `cobbledollars pay @s <fee>` as the player is a net-zero self-pay whose
      // RESULT is the amount when affordable and 0 (soft-fail) when broke — `store success` would
      // read 1 either way, so we capture the result via withCallback. Valid ONLY because this runs
      // from a plain tick path (see PENDING_CONFIRMS: nested dispatch would defer the callback).
      final int[] paid = {0};
      CommandSourceStack probe = sp.createCommandSourceStack()
        .withSuppressedOutput().withPermission(2)
        .withCallback((success, result) -> paid[0] = result);
      server.getCommands().performPrefixedCommand(probe, "cobbledollars pay @s " + fee);
      if (paid[0] < 1) {
        sp.displayClientMessage(Component.literal(
          "§cYou cannot cover the owner\u0027s rate. §7(" + fee + " CD required)"), true);
        return;
      }
      server.getCommands().performPrefixedCommand(
        server.createCommandSourceStack().withSuppressedOutput().withPermission(2),
        "cobbledollars remove " + sp.getScoreboardName() + " " + fee);
      LAST_CHARGED.put(key(sp, pos), now);
      sp.displayClientMessage(Component.literal(
        "§6You leave §e" + fee + " CD§6 for the owner. §7The tools are yours for a minute."), true);
    }

    MenuProvider provider = state.getMenuProvider(level, pos);
    if (provider != null) sp.openMenu(provider);
  }

  /** The owner's rate for this station type, scaled by the ModMenu Economy multiplier. */
  private static int scaledFee(Block block) {
    int base = FEE_BY_BLOCK.getOrDefault(block, 200);
    return Math.max(1, base * com.thecompanyinc.cobblemoninitiative.config.EconomyConfig
      .get().getFeeMultiplierPercent() / 100);
  }

  /** Mirror the fee multiplier into {@code #cfg_fee_mult cd_const} for the heal_paid datapack
   *  math (creates the objective if the datapack load has not run yet — idempotent). */
  private static void assertFeeMultiplier(MinecraftServer server) {
    net.minecraft.world.scores.Scoreboard sb = server.getScoreboard();
    net.minecraft.world.scores.Objective obj = sb.getObjective("cd_const");
    if (obj == null) {
      obj = sb.addObjective("cd_const", net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY,
        Component.literal("cd_const"),
        net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER, false, null);
    }
    sb.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly("#cfg_fee_mult"), obj)
      .set(com.thecompanyinc.cobblemoninitiative.config.EconomyConfig.get().getFeeMultiplierPercent());
  }

  private static String key(ServerPlayer sp, BlockPos pos) {
    return sp.getUUID() + ":" + pos.asLong();
  }

}

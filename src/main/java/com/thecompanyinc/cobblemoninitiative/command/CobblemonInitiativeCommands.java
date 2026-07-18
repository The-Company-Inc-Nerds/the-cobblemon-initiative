package com.thecompanyinc.cobblemoninitiative.command;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.config.LootChestConfig;
import com.thecompanyinc.cobblemoninitiative.config.ProgressionConfig;
import com.thecompanyinc.cobblemoninitiative.config.ShrineConfig;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import com.thecompanyinc.cobblemoninitiative.economy.ShopTierManager;
import com.thecompanyinc.cobblemoninitiative.npcsight.NpcSightInit;
import com.thecompanyinc.cobblemoninitiative.stadium.StadiumManager;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CobblemonInitiativeCommands {

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher
  ) {
    dispatcher.register(
      Commands.literal("cobblemon-initiative")
        // No root permission gate: the player-facing `track` subtree below must work at
        // permission 0 (mirrors /shrine-abort). Every admin subtree carries its own
        // OP-2 requires instead — brigadier keeps the FIRST registered node's
        // requirement on merge, so a second permission-free registration would not do.
        .then(
          Commands.literal("progress")
            .requires(source -> source.hasPermission(2))
            .executes(CobblemonInitiativeCommands::showProgress)
        )
        .then(
          Commands.literal("levelcap")
            .requires(source -> source.hasPermission(2))
            .executes(CobblemonInitiativeCommands::showLevelCap)
        )
        .then(
          Commands.literal("reset")
            .requires(source -> source.hasPermission(2))
            .executes(CobblemonInitiativeCommands::resetProgress)
        )
        // Species-verified party trade (Cobblemon API): removes the FIRST party Pokémon
        // matching <take> and gives a <give> in its place. If the player has no <take>,
        // it's a safe no-op. OP-2 so only NPC dialogs (perm-2 context) can call it, never
        // a player cheesing a free upgrade. /cobblemon-initiative trade <take> <give> [level]
        .then(
          Commands.literal("trade")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("take", StringArgumentType.word())
                .then(
                  Commands.argument("give", StringArgumentType.word())
                    .executes(ctx -> trade(
                      ctx,
                      StringArgumentType.getString(ctx, "take"),
                      StringArgumentType.getString(ctx, "give"),
                      -1,
                      null
                    ))
                    .then(
                      Commands.argument("level", IntegerArgumentType.integer(1, 100))
                        .executes(ctx -> trade(
                          ctx,
                          StringArgumentType.getString(ctx, "take"),
                          StringArgumentType.getString(ctx, "give"),
                          IntegerArgumentType.getInteger(ctx, "level"),
                          null
                        ))
                        .then(
                          Commands.argument("tag", StringArgumentType.word())
                            .executes(ctx -> trade(
                              ctx,
                              StringArgumentType.getString(ctx, "take"),
                              StringArgumentType.getString(ctx, "give"),
                              IntegerArgumentType.getInteger(ctx, "level"),
                              StringArgumentType.getString(ctx, "tag")
                            ))
                        )
                    )
                )
            )
        )
        // Item turn-in helper: counts <item> in the player's inventory; if there are at least
        // <count>, removes exactly that many and (optionally) adds <success_tag>. Safe no-op
        // with a "you need N" message otherwise. Replaces the broken has_item dialog gate and
        // the per-quest clear-dry-run functions. /cobblemon-initiative turnin <item> <count> [tag]
        .then(
          Commands.literal("turnin")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("item", ResourceLocationArgument.id())
                .then(
                  Commands.argument("count", IntegerArgumentType.integer(1))
                    .executes(ctx -> turnin(
                      ctx,
                      ResourceLocationArgument.getId(ctx, "item"),
                      IntegerArgumentType.getInteger(ctx, "count"),
                      null
                    ))
                    .then(
                      Commands.argument("tag", StringArgumentType.word())
                        .executes(ctx -> turnin(
                          ctx,
                          ResourceLocationArgument.getId(ctx, "item"),
                          IntegerArgumentType.getInteger(ctx, "count"),
                          StringArgumentType.getString(ctx, "tag")
                        ))
                    )
                )
            )
        )
        // Verified party give (Cobblemon API): parses a FULL PokemonProperties string and adds
        // the result to the party, resolving the player from the command SOURCE so it works in
        // both dialog-button and function contexts. Unifies every Pokémon gift here instead of
        // givepokemonother (which works in dialog buttons but failed on a raw @s inside a
        // function). Accepts anything PokemonProperties understands — level=, shiny=, gender=,
        // hisuian=, etc.
        // /cobblemon-initiative givemon <properties>   e.g.  eevee level=5 shiny=true
        .then(
          Commands.literal("givemon")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("properties", StringArgumentType.greedyString())
                .executes(ctx -> givemon(ctx, StringArgumentType.getString(ctx, "properties")))
            )
        )
        .then(
          Commands.literal("track")
            // No parse-time player requirement: Brigadier evaluates .requires() against
            // the ORIGINAL source, so console `execute as <player> run … track next`
            // (the headless harness / Carpet fake-player path) never saw the subtree.
            // The handlers resolve the player at runtime and no-op without one.
            .then(
              Commands.literal("next").executes(ctx -> trackCycle(ctx, 1))
            )
            .then(
              Commands.literal("prev").executes(ctx -> trackCycle(ctx, -1))
            )
            .then(
              Commands.literal("clear").executes(
                CobblemonInitiativeCommands::trackClear
              )
            )
            .then(
              Commands.literal("status").executes(
                CobblemonInitiativeCommands::trackStatus
              )
            )
        )
        // Dishonorable Respawn — fired by the PokeballDeathScreen button. Perm 0, resolves
        // the (dead) player from the source at runtime. Brings them back in SURVIVAL while
        // leaving hardcore armed, and brands them permanently.
        .then(
          Commands.literal("dishonored-respawn")
            .executes(CobblemonInitiativeCommands::dishonoredRespawn)
        )
        // Daycare — player-facing (perm 0) AND dialog-button-ready: like `track`, targets
        // resolve at runtime (getPlayer() null-check), with NO parse-time entity requires —
        // an Easy NPC ExecAsUser source carries the player, but requires() is evaluated
        // when the command tree is built/suggested, and a failed check there silently
        // hides the node from non-player dispatch contexts (functions, dialog rails).
        .then(
          Commands.literal("daycare")
            .then(
              Commands.literal("deposit").executes(
                CobblemonInitiativeCommands::daycareDeposit
              )
            )
            .then(
              Commands.literal("withdraw").then(
                Commands.argument(
                  "slot",
                  IntegerArgumentType.integer(
                    1,
                    com.thecompanyinc.cobblemoninitiative.daycare.DaycareManager.MAX_SLOTS
                  )
                ).executes(ctx -> daycareWithdraw(
                  ctx,
                  IntegerArgumentType.getInteger(ctx, "slot")
                ))
              )
            )
            .then(
              Commands.literal("status").executes(
                CobblemonInitiativeCommands::daycareStatus
              )
            )
        )
        // Homestead beacons — player-facing (runtime player resolution, like `track`), so
        // a dialog button (`homestead claim/buy`) works via an Easy NPC ExecAsUser source.
        .then(
          Commands.literal("homestead")
            .then(Commands.literal("claim").executes(CobblemonInitiativeCommands::homesteadClaim))
            .then(Commands.literal("unclaim").executes(CobblemonInitiativeCommands::homesteadUnclaim))
            .then(Commands.literal("buy").executes(CobblemonInitiativeCommands::homesteadBuy))
            .then(Commands.literal("status").executes(CobblemonInitiativeCommands::homesteadStatus))
        )
        // Mom's friendship care — 1-slot boarding, player-facing (dialog buttons).
        .then(
          Commands.literal("momcare")
            .then(Commands.literal("deposit").executes(CobblemonInitiativeCommands::momCareDeposit))
            .then(Commands.literal("withdraw").executes(CobblemonInitiativeCommands::momCareWithdraw))
            .then(Commands.literal("status").executes(CobblemonInitiativeCommands::momCareStatus))
        )
        // Stadium exhibition circuit — player-facing, permission 0 (mirrors `track`).
        // Deliberately NO parse-time .requires(entity instanceof ServerPlayer): the
        // player resolves at RUNTIME so a console `execute as <player> run …` works
        // (headless test harness); a non-player source just gets a failure message.
        .then(
          Commands.literal("stadium")
            .then(
              Commands.literal("start")
                .then(Commands.literal("25").executes(ctx -> stadiumStart(ctx, 25)))
                .then(Commands.literal("50").executes(ctx -> stadiumStart(ctx, 50)))
                .then(Commands.literal("75").executes(ctx -> stadiumStart(ctx, 75)))
                .then(Commands.literal("100").executes(ctx -> stadiumStart(ctx, 100)))
            )
            .then(
              Commands.literal("abort").executes(
                CobblemonInitiativeCommands::stadiumAbort
              )
            )
            .then(
              Commands.literal("status").executes(
                CobblemonInitiativeCommands::stadiumStatus
              )
            )
        )
        // Safari Zone — the Baiting Yards. enter/exit/status are PLAYER-FACING (perm 0,
        // runtime player resolution like `track` — no parse-time requires so kiosk
        // dialog buttons and functions can dispatch them as the player). bait is the
        // perm-2 kiosk give (ExecAsUser dialog buttons run elevated, source = player).
        .then(
          Commands.literal("safari")
            .then(
              Commands.literal("enter").executes(CobblemonInitiativeCommands::safariEnter)
            )
            .then(
              Commands.literal("exit").executes(CobblemonInitiativeCommands::safariExit)
            )
            .then(
              Commands.literal("status").executes(CobblemonInitiativeCommands::safariStatus)
            )
            .then(
              Commands.literal("bait")
                .requires(source -> source.hasPermission(2))
                .then(
                  Commands.argument("type", StringArgumentType.word())
                    .suggests((context, builder) ->
                      SharedSuggestionProvider.suggest(
                        InitiativeInit.getSafariManager().getBaitTypes(),
                        builder
                      )
                    )
                    .executes(ctx -> safariBait(
                      ctx,
                      StringArgumentType.getString(ctx, "type"),
                      1
                    ))
                    .then(
                      Commands.argument("count", IntegerArgumentType.integer(1, 64))
                        .executes(ctx -> safariBait(
                          ctx,
                          StringArgumentType.getString(ctx, "type"),
                          IntegerArgumentType.getInteger(ctx, "count")
                        ))
                    )
                )
            )
            .then(
              // Dev/test hook: drives the same scatter path as the bait right-click
              // (Carpet bots can't fire UseBlockCallback — headless verification).
              Commands.literal("scatter")
                .requires(source -> source.hasPermission(2))
                .then(
                  Commands.argument("type", StringArgumentType.word())
                    .suggests((context, builder) ->
                      SharedSuggestionProvider.suggest(
                        InitiativeInit.getSafariManager().getBaitTypes(),
                        builder
                      )
                    )
                    .executes(ctx -> {
                      ServerPlayer player = ctx.getSource().getPlayer();
                      if (player == null) return 0;
                      boolean ok = InitiativeInit.getSafariManager()
                        .devScatter(player, StringArgumentType.getString(ctx, "type"));
                      ctx.getSource().sendSuccess(
                        () -> Component.literal(ok
                          ? "[Safari] scatter queued at the player's feet."
                          : "[Safari] scatter refused (no session or unknown bait)."),
                        false
                      );
                      return ok ? 1 : 0;
                    })
                )
            )
        )
        .then(
          Commands.literal("info")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("trainer", StringArgumentType.string())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(
                    InitiativeInit.getConfigLoader().getTrainerIds(),
                    builder
                  )
                )
                .executes(CobblemonInitiativeCommands::showTrainerInfo)
            )
        )
        .then(
          Commands.literal("list")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.literal("gyms").executes(ctx -> listTrainers(ctx, "gym"))
            )
            .then(
              Commands.literal("shrines").executes(ctx ->
                listTrainers(ctx, "shrine")
              )
            )
            .then(
              Commands.literal("groups").executes(
                CobblemonInitiativeCommands::listGroups
              )
            )
            .then(
              Commands.literal("all").executes(ctx -> listTrainers(ctx, null))
            )
        )
        .then(
          Commands.literal("defeat")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("trainer", StringArgumentType.string())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(
                    InitiativeInit.getConfigLoader().getTrainerIds(),
                    builder
                  )
                )
                .executes(CobblemonInitiativeCommands::markDefeated)
            )
        )
        .then(
          Commands.literal("shrine")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("shrine", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(
                    Arrays.asList(
                      InitiativeInit.getShrineChallengeManager().getShrineIds()
                    ),
                    builder
                  )
                )
                .then(
                  Commands.literal("start").executes(
                    CobblemonInitiativeCommands::shrineStart
                  )
                )
                .then(
                  Commands.literal("stop").executes(
                    CobblemonInitiativeCommands::shrineStop
                  )
                )
                .then(
                  Commands.literal("test").then(
                    Commands.argument("testName", StringArgumentType.word())
                      .suggests((context, builder) ->
                        SharedSuggestionProvider.suggest(
                          List.of("friendship", "fullness", "nickname", "shiny", "resolve"),
                          builder
                        )
                      )
                      .executes(CobblemonInitiativeCommands::shrineTest)
                  )
                )
                .then(
                  Commands.literal("complete").executes(
                    CobblemonInitiativeCommands::shrineComplete
                  )
                )
                .then(
                  Commands.literal("path")
                    .then(
                      Commands.literal("record").executes(
                        CobblemonInitiativeCommands::shrinePathRecord
                      )
                    )
                    .then(
                      Commands.literal("here").executes(
                        CobblemonInitiativeCommands::shrinePathHere
                      )
                    )
                    .then(
                      Commands.literal("clear").executes(
                        CobblemonInitiativeCommands::shrinePathClear
                      )
                    )
                    .then(
                      Commands.literal("show").executes(
                        CobblemonInitiativeCommands::shrinePathShow
                      )
                    )
                    .then(
                      Commands.literal("export").executes(
                        CobblemonInitiativeCommands::shrinePathExport
                      )
                    )
                )
            )
        )
        .then(
          Commands.literal("quest")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("show").executes(CobblemonInitiativeCommands::questShow))
            .then(Commands.literal("hide").executes(CobblemonInitiativeCommands::questHide))
            .then(Commands.literal("refresh").executes(CobblemonInitiativeCommands::questRefresh))
        )
        .then(
          Commands.literal("reload")
            .requires(source -> source.hasPermission(2))
            .executes(CobblemonInitiativeCommands::reloadAll)
        )
        .then(
          Commands.literal("shop")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.literal("refresh").executes(CobblemonInitiativeCommands::refreshShopTier)
            )
            .then(
              Commands.argument("tier", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(ShopTierManager.TIERS, builder)
                )
                .executes(CobblemonInitiativeCommands::applyShopTier)
            )
        )
        .then(TestCommands.build())
    );

    dispatcher.register(
      Commands.literal("ca")
        .requires(source -> source.hasPermission(2))
        .redirect(dispatcher.getRoot().getChild("cobblemon-initiative"))
    );
  }

  // Quest HUD toggle — thin wrappers that dispatch the datapack quest functions
  // (the sidebar HUD logic lives in data/.../function/quest/).
  private static int questDispatch(
    CommandContext<CommandSourceStack> context, String fn
  ) {
    context.getSource().getServer().getCommands().performPrefixedCommand(
      context.getSource(), "function cobblemon_initiative:quest/" + fn
    );
    return 1;
  }

  private static int questShow(CommandContext<CommandSourceStack> context) {
    return questDispatch(context, "show");
  }

  private static int questHide(CommandContext<CommandSourceStack> context) {
    return questDispatch(context, "hide");
  }

  private static int questRefresh(CommandContext<CommandSourceStack> context) {
    return questDispatch(context, "refresh");
  }

  // ── Safari Zone (the Baiting Yards) ───────────────────────────────────────────

  /** /cobblemon-initiative safari enter — badge gate → pay-probe → session start. */
  private static int safariEnter(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    return InitiativeInit.getSafariManager().enter(player) ? 1 : 0;
  }

  /** /cobblemon-initiative safari exit — voluntary end: clawback + visit ledger. */
  private static int safariExit(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    return InitiativeInit.getSafariManager().exitVoluntary(player) ? 1 : 0;
  }

  /** /cobblemon-initiative safari status — clock, issued balls, ledger, warm spots. */
  private static int safariStatus(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    return InitiativeInit.getSafariManager().status(player);
  }

  /** /cobblemon-initiative safari bait <type> [count] — perm-2 kiosk give. */
  private static int safariBait(
    CommandContext<CommandSourceStack> context,
    String type,
    int count
  ) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    return InitiativeInit.getSafariManager().giveBait(player, type, count) ? 1 : 0;
  }

  // ── Quest tracking (player-facing, permission 0 — driven by the ] / [ keybinds) ──

  /** /cobblemon-initiative track next|prev — cycle the tracked sidebar quest. */
  private static int trackCycle(
    CommandContext<CommandSourceStack> context, int direction
  ) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) return 0;
    InitiativeInit.getQuestTrackManager().cycle(player, direction);
    return 1;
  }

  /** /cobblemon-initiative track clear — stop tracking. */
  private static int trackClear(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) return 0;
    InitiativeInit.getQuestTrackManager().clearTracking(player);
    return 1;
  }

  /** /cobblemon-initiative track status — list active quests, tracked one marked. */
  private static int trackStatus(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) return 0;
    InitiativeInit.getQuestTrackManager().sendStatus(player);
    return 1;
  }

  // ── Daycare (player-facing, permission 0 — dialog-button-ready) ────────────────

  /**
   * /cobblemon-initiative daycare deposit — the Sango keeper's "Board a Pokémon" button.
   * Validates pen capacity + the last-mon rule server-side, then raises the pending-picker
   * flag; the client tick poll opens DaycareSelectionScreen once no other screen is up
   * (same split as the sacrifice flow). The screen's confirm re-enters the server through
   * DaycareManager.deposit, which re-validates everything.
   */
  private static int daycareDeposit(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    var daycare = InitiativeInit.getDaycareManager();
    if (!daycare.getConfig().isEnabled()) {
      player.sendSystemMessage(Component.literal("§cThe daycare is closed."));
      return 0;
    }
    if (
      daycare.boardedCount(player.getUUID()) >=
      com.thecompanyinc.cobblemoninitiative.daycare.DaycareManager.MAX_SLOTS
    ) {
      player.sendSystemMessage(Component.literal("§cYour daycare pens are already full."));
      return 0;
    }
    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    int partySize = 0;
    for (Pokemon p : party) {
      if (p != null) partySize++;
    }
    if (partySize <= 1) {
      player.sendSystemMessage(Component.literal("§cYou cannot board your last Pokémon."));
      return 0;
    }
    com.thecompanyinc.cobblemoninitiative.daycare.DaycareManager.triggerPicker();
    return 1;
  }

  /** /cobblemon-initiative daycare withdraw <slot> — pay the pickup fee, get the mon back. */
  private static int daycareWithdraw(
    CommandContext<CommandSourceStack> context, int slot
  ) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getDaycareManager().withdraw(player, slot - 1);
    return 1;
  }

  /** /cobblemon-initiative daycare status — boarded mons, growth, live pickup fees. */
  private static int daycareStatus(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getDaycareManager().sendStatus(player);
    return 1;
  }

  // ── Homestead beacons (player-facing) ────────────────────────────────────────

  /** /cobblemon-initiative homestead claim — register the nearest beacon as a homestead. */
  private static int homesteadClaim(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getHomesteadManager().claimNearestBeacon(player);
    return 1;
  }

  /** /cobblemon-initiative homestead unclaim — release the nearest registered homestead. */
  private static int homesteadUnclaim(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getHomesteadManager().unclaimNearestBeacon(player);
    return 1;
  }

  /** /cobblemon-initiative homestead buy — buy the next beacon from Suzune (escalating price). */
  private static int homesteadBuy(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getHomesteadManager().buyBeacon(player);
    return 1;
  }

  /** /cobblemon-initiative homestead status — claimed fields, tiers, daily harvest. */
  private static int homesteadStatus(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getHomesteadManager().sendStatus(player);
    return 1;
  }

  // ── Mom's friendship care (player-facing) ────────────────────────────────────

  /** /cobblemon-initiative momcare deposit — open the 1-slot picker to leave a mon with Mom. */
  private static int momCareDeposit(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    var mom = InitiativeInit.getMomCareManager();
    if (!mom.getConfig().isEnabled()) {
      player.sendSystemMessage(Component.literal("§cMom isn't taking Pokémon right now."));
      return 0;
    }
    if (mom.boardedCount(player.getUUID()) >= com.thecompanyinc.cobblemoninitiative.momcare.MomCareManager.MAX_SLOTS) {
      player.sendSystemMessage(Component.literal("§cMom already has one of your Pokémon."));
      return 0;
    }
    com.thecompanyinc.cobblemoninitiative.momcare.MomCareManager.triggerPicker();
    return 1;
  }

  /** /cobblemon-initiative momcare withdraw — bring your mon home from Mom. */
  private static int momCareWithdraw(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getMomCareManager().withdraw(player);
    return 1;
  }

  /** /cobblemon-initiative momcare status — who Mom is watching + current friendship. */
  private static int momCareStatus(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getMomCareManager().sendStatus(player);
    return 1;
  }

  // ── Stadium exhibition circuit (player-facing, permission 0) ─────────────────

  /** Runtime player resolution shared by the stadium handlers (no parse-time gate). */
  private static ServerPlayer stadiumPlayer(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(
        Component.literal("§cThe stadium needs a player source (use execute as <player>).")
      );
    }
    return player;
  }

  /** /cobblemon-initiative stadium start <25|50|75|100> */
  private static int stadiumStart(CommandContext<CommandSourceStack> context, int bracket) {
    ServerPlayer player = stadiumPlayer(context);
    if (player == null) return 0;
    StadiumManager.startRun(player, bracket);
    return 1;
  }

  /** /cobblemon-initiative stadium abort — end the run (refused while a bout is live). */
  private static int stadiumAbort(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = stadiumPlayer(context);
    if (player == null) return 0;
    StadiumManager.abortRun(player);
    return 1;
  }

  /** /cobblemon-initiative stadium status — bracket, wave, and phase of the run. */
  private static int stadiumStatus(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = stadiumPlayer(context);
    if (player == null) return 0;
    StadiumManager.sendStatus(player);
    return 1;
  }

  /**
   * /cobblemon-initiative shop &lt;tier&gt;
   * Swaps the CobbleDollars default shop to a pre-baked tier and hot-reloads it. Invoked from gym
   * leader / Acting CEO DJ reward commands as the badge progresses; also a manual admin/test lever.
   */
  private static int applyShopTier(CommandContext<CommandSourceStack> context) {
    String tier = StringArgumentType.getString(context, "tier");
    boolean ok = ShopTierManager.applyTier(context.getSource().getServer(), tier);
    if (ok) {
      context.getSource().sendSuccess(
        () ->
          Component.literal(
            "§a[Shop] Applied tier §e" + tier + " §aand reloaded CobbleDollars."
          ),
        true
      );
      return 1;
    }
    context.getSource().sendFailure(
      Component.literal("§cUnknown or unreadable shop tier: " + tier)
    );
    return 0;
  }

  /**
   * /cobblemon-initiative shop refresh — re-apply the source player's current base tier
   * so the liberation-relief level (fields_liberated) is re-resolved. Fired by
   * liberation/free_field_apply after each field liberation; safe to run any time.
   */
  private static int refreshShopTier(CommandContext<CommandSourceStack> context) {
    net.minecraft.server.level.ServerPlayer player;
    try {
      player = context.getSource().getPlayerOrException();
    } catch (Exception e) {
      context.getSource().sendFailure(
        Component.literal("§cshop refresh needs a player source (it reads badge/field state).")
      );
      return 0;
    }
    String base = ShopTierManager.currentBaseTier(player);
    boolean ok = ShopTierManager.applyTier(context.getSource().getServer(), base);
    if (ok) {
      context.getSource().sendSuccess(
        () -> Component.literal("§a[Shop] Re-applied tier for §e" + base + "§a (relief re-resolved)."),
        true
      );
      return 1;
    }
    context.getSource().sendFailure(Component.literal("§cCould not re-apply tier " + base));
    return 0;
  }

  private static int listGroups(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();

    source.sendSuccess(
      () -> Component.literal("§6=== Available Groups ==="),
      false
    );

    for (String group : InitiativeInit.getConfigLoader().getAvailableGroups()) {
      List<TrainerConfig> trainers =
        InitiativeInit.getConfigLoader().getTrainersByGroup(group);
      source.sendSuccess(
        () ->
          Component.literal(
            "§e" + group + " §7(" + trainers.size() + " trainers)"
          ),
        false
      );
    }

    return 1;
  }

  /**
   * Species-verified party trade. Finds the first party Pokémon whose species name matches
   * {@code take} (case-insensitive), removes it, and adds a freshly-created {@code give} in
   * its place. {@code level <= 0} means "match the traded Pokémon's level". If {@code successTag}
   * is non-null it is added to the player ONLY on a completed trade — so the caller's quest
   * latch stays all-or-nothing. Safe no-op with a message when the player has no {@code take}.
   * Backs the NPC trade dialogs (e.g. Old Sefu's Magikarp→Feebas) so no fixed party slot is
   * assumed and nothing else is ever removed.
   */
  private static int trade(
    CommandContext<CommandSourceStack> context,
    String take,
    String give,
    int level,
    String successTag
  ) {
    ServerPlayer player;
    try {
      player = context.getSource().getPlayerOrException();
    } catch (Exception e) {
      return 0;
    }

    PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
    Pokemon toTake = null;
    for (Pokemon p : party) {
      if (p != null && p.getSpecies().getName().equalsIgnoreCase(take)) {
        toTake = p;
        break;
      }
    }
    if (toTake == null) {
      player.sendSystemMessage(
        Component.literal("§cYou have no " + take + " to trade.")
      );
      return 0;
    }

    int giveLevel = level > 0 ? level : toTake.getLevel();
    party.remove(toTake);
    giveSpecies(player, give, giveLevel);
    if (successTag != null && !successTag.isBlank()) {
      player.addTag(successTag);
    }

    player.sendSystemMessage(
      Component.literal("§aTraded your " + take + " for a " + give + ".")
    );
    return 1;
  }

  /**
   * Item turn-in helper. Counts {@code item} across the player's inventory; if there are at
   * least {@code count}, removes exactly that many and (when non-null) adds {@code successTag}.
   * Otherwise a safe no-op with a "you need N" message. Backs the NPC hand-in buttons so the
   * broken Easy NPC has_item condition is never relied on and a reward can gate on the tag.
   */
  private static int turnin(
    CommandContext<CommandSourceStack> context,
    ResourceLocation itemId,
    int count,
    String successTag
  ) {
    ServerPlayer player;
    try {
      player = context.getSource().getPlayerOrException();
    } catch (Exception e) {
      return 0;
    }

    Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
    if (item == null || item == Items.AIR) {
      player.sendSystemMessage(Component.literal("§cUnknown item: " + itemId));
      return 0;
    }

    Inventory inv = player.getInventory();
    int have = 0;
    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack s = inv.getItem(i);
      if (s.is(item)) have += s.getCount();
    }

    String pretty = itemId.getPath().replace('_', ' ');
    if (have < count) {
      player.sendSystemMessage(
        Component.literal("§cYou need " + count + " " + pretty + " (you have " + have + ").")
      );
      return 0;
    }

    int toRemove = count;
    for (int i = 0; i < inv.getContainerSize() && toRemove > 0; i++) {
      ItemStack s = inv.getItem(i);
      if (s.is(item)) {
        int take = Math.min(toRemove, s.getCount());
        s.shrink(take);
        toRemove -= take;
      }
    }

    if (successTag != null && !successTag.isBlank()) {
      player.addTag(successTag);
    }
    player.sendSystemMessage(Component.literal("§aHanded in " + count + " " + pretty + "."));
    return 1;
  }

  /**
   * Verified party give (Cobblemon API). Parses a full {@code PokemonProperties} string (species
   * + any of level= / shiny= / gender= / hisuian= …) and adds the result to the player's party.
   * Resolves the player from the command source, so it works in both dialog-button and function
   * contexts — unlike a raw {@code @s givepokemonother}, which failed inside a .mcfunction.
   */
  private static int givemon(CommandContext<CommandSourceStack> context, String properties) {
    ServerPlayer player;
    try {
      player = context.getSource().getPlayerOrException();
    } catch (Exception e) {
      return 0;
    }

    Pokemon given = giveProperties(player, properties);
    if (given == null) {
      player.sendSystemMessage(Component.literal("§cCould not create Pokémon: " + properties));
      return 0;
    }
    player.sendSystemMessage(
      Component.literal("§a" + given.getSpecies().getName() + " joined your team.")
    );
    return 1;
  }

  /** Parse a full PokemonProperties string, create the mon, and add it to the party. */
  private static Pokemon giveProperties(ServerPlayer player, String properties) {
    try {
      Pokemon mon = PokemonProperties.Companion.parse(properties).create();
      Cobblemon.INSTANCE.getStorage().getParty(player).add(mon);
      return mon;
    } catch (Exception e) {
      return null;
    }
  }

  /** Create {@code species} at {@code level} and add it to the party (used by {@code trade}). */
  private static Pokemon giveSpecies(ServerPlayer player, String species, int level) {
    return giveProperties(player, species + " level=" + level);
  }

  private static int showProgress(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();

    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    PlayerProgress progress =
      InitiativeInit.getProgressManager().getProgress(player);

    source.sendSuccess(
      () -> Component.literal("§6=== Your Progress ==="),
      false
    );
    source.sendSuccess(
      () ->
        Component.literal(
          "§eTrainers Defeated: §f" + progress.getDefeatedTrainers().size()
        ),
      false
    );
    source.sendSuccess(
      () ->
        Component.literal(
          "§eAchievements Earned: §f" + progress.getEarnedAchievements().size()
        ),
      false
    );
    source.sendSuccess(
      () ->
        Component.literal(
          "§eCurrent Level Cap: §f" + progress.getCurrentLevelCap()
        ),
      false
    );

    source.sendSuccess(() -> Component.literal("§6--- Gym Badges ---"), false);
    List<TrainerConfig> gymLeaders =
      InitiativeInit.getConfigLoader().getTrainersByCategory("gym").stream()
        .filter(t -> "leader".equals(t.getTrainerType()))
        .toList();
    int badges = (int) gymLeaders.stream()
      .filter(t -> progress.hasDefeatedTrainer(t.getId()))
      .count();
    final int totalGymBadges = gymLeaders.size();
    final int finalBadges = badges;
    source.sendSuccess(
      () -> Component.literal("§eBadges: §f" + finalBadges + "/" + totalGymBadges),
      false
    );

    return 1;
  }

  private static int showLevelCap(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();

    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    int levelCap = InitiativeInit.getLevelCapManager().getLevelCap(player);
    source.sendSuccess(
      () -> Component.literal("§eYour current level cap is: §f" + levelCap),
      false
    );

    return 1;
  }

  private static int resetProgress(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();

    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    PlayerProgress progress =
      InitiativeInit.getProgressManager().getProgress(player);
    progress.getDefeatedTrainers().clear();
    progress.getEarnedAchievements().clear();
    progress.setCurrentLevelCap(ProgressionConfig.get().getBaseLevelCap());

    if (player.getServer() != null) {
      InitiativeInit.getProgressManager().saveProgress(player.getServer());
    }

    source.sendSuccess(
      () -> Component.literal("§aYour progress has been reset!"),
      false
    );

    return 1;
  }

  private static int showTrainerInfo(
    CommandContext<CommandSourceStack> context
  ) {
    CommandSourceStack source = context.getSource();
    String trainerId = StringArgumentType.getString(context, "trainer");

    TrainerConfig trainer = InitiativeInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) {
      source.sendFailure(Component.literal("Unknown trainer: " + trainerId));
      return 0;
    }

    source.sendSuccess(
      () -> Component.literal("§6=== " + trainer.getDisplayName() + " ==="),
      false
    );
    source.sendSuccess(
      () -> Component.literal("§eID: §f" + trainer.getId()),
      false
    );
    source.sendSuccess(
      () -> Component.literal("§eCategory: §f" + trainer.getCategory()),
      false
    );
    source.sendSuccess(
      () -> Component.literal("§eLocation: §f" + trainer.getLocation()),
      false
    );
    source.sendSuccess(
      () -> Component.literal("§eType: §f" + trainer.getTrainerType()),
      false
    );

    if (trainer.getGroup() != null) {
      source.sendSuccess(
        () -> Component.literal("§eGroup: §f" + trainer.getGroup()),
        false
      );
    }

    if (
      trainer.getCoordinates() != null && trainer.getCoordinates().length == 3
    ) {
      source.sendSuccess(
        () ->
          Component.literal(
            "§eCoordinates: §f" +
              trainer.getCoordinates()[0] +
              ", " +
              trainer.getCoordinates()[1] +
              ", " +
              trainer.getCoordinates()[2]
          ),
        false
      );
    }

    if (!trainer.getPrerequisites().isEmpty()) {
      source.sendSuccess(() -> Component.literal("§ePrerequisites:"), false);
      for (String prereq : trainer.getPrerequisites()) {
        TrainerConfig prereqTrainer =
          InitiativeInit.getConfigLoader().getTrainer(prereq);
        String name =
          prereqTrainer != null ? prereqTrainer.getDisplayName() : prereq;
        source.sendSuccess(() -> Component.literal("§7  - " + name), false);
      }
    }

    if (trainer.getSpawnOnDefeat() != null) {
      source.sendSuccess(
        () ->
          Component.literal(
            "§eSpawns on Defeat: §f" +
              trainer.getSpawnOnDefeat().getSpecies() +
              " (Lv. " +
              trainer.getSpawnOnDefeat().getLevel() +
              ")"
          ),
        false
      );
    }

    if (trainer.getTeam() != null && trainer.getTeam().getPokemon() != null) {
      source.sendSuccess(
        () ->
          Component.literal(
            "§eTeam Size: §f" +
              trainer.getTeam().getPokemon().size() +
              " Pokemon"
          ),
        false
      );
    }

    return 1;
  }

  private static int listTrainers(
    CommandContext<CommandSourceStack> context,
    String category
  ) {
    CommandSourceStack source = context.getSource();

    source.sendSuccess(
      () ->
        Component.literal(
          "§6=== Trainers" +
            (category != null ? " (" + category + ")" : "") +
            " ==="
        ),
      false
    );

    for (TrainerConfig trainer : InitiativeInit.getConfigLoader().getAllTrainers()) {
      if (category == null || trainer.getCategory().contains(category)) {
        source.sendSuccess(
          () ->
            Component.literal(
              "§e" + trainer.getId() + " §7- " + trainer.getDisplayName()
            ),
          false
        );
      }
    }

    return 1;
  }

  private static int markDefeated(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    String trainerId = StringArgumentType.getString(context, "trainer");

    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(
        Component.literal("This command must be run by a player")
      );
      return 0;
    }

    TrainerConfig trainer = InitiativeInit.getConfigLoader().getTrainer(
      trainerId
    );
    if (trainer == null) {
      source.sendFailure(Component.literal("Unknown trainer: " + trainerId));
      return 0;
    }

    InitiativeInit.getProgressManager().onTrainerDefeated(player, trainerId);
    source.sendSuccess(
      () ->
        Component.literal(
          "§aMarked §e" + trainer.getDisplayName() + " §aas defeated!"
        ),
      true
    );

    return 1;
  }

  // ── Shrine challenge commands ─────────────────────────────────────────────────

  /**
   * /cobblemon-initiative shrine <id> start
   * Intended to be triggered from an Easy NPC dialog or a pressure plate.
   */
  private static int shrineStart(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String shrineId = StringArgumentType.getString(context, "shrine");
    boolean started = InitiativeInit.getShrineChallengeManager().startChallenge(
      player,
      shrineId
    );
    return started ? 1 : 0;
  }

  /**
   * /cobblemon-initiative shrine <id> stop
   * Aborts the active challenge (no penalty). Also registered as /shrine-abort.
   */
  private static int shrineStop(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    InitiativeInit.getShrineChallengeManager().stopChallenge(player);
    return 1;
  }

  /**
   * /cobblemon-initiative shrine fairy test <testName>
   * Runs an individual Fairy shrine test on the player's lead Pokémon.
   * testName: friendship | fullness | nickname | shiny | resolve
   * Triggered from an Easy NPC altar dialog or command block.
   */
  private static int shrineTest(CommandContext<CommandSourceStack> context) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String testName = StringArgumentType.getString(context, "testName");
    boolean passed = InitiativeInit.getShrineChallengeManager().runFairyTest(player, testName);
    return passed ? 1 : 0;
  }

  /**
   * /cobblemon-initiative shrine <id> complete
   * Called by a command block at the parkour finish line:
   *   execute as @a[distance=..3] run cobblemon-initiative shrine fire complete
   */
  private static int shrineComplete(
    CommandContext<CommandSourceStack> context
  ) {
    CommandSourceStack source = context.getSource();
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      source.sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String shrineId = StringArgumentType.getString(context, "shrine");
    boolean completed =
      InitiativeInit.getShrineChallengeManager().completeParkour(
        player,
        shrineId
      );
    return completed ? 1 : 0;
  }

  // ── Safe-path authoring (dev) ─────────────────────────────────────────────────

  /** Resolves the player + shrine id shared by every path subcommand. */
  private static ServerPlayer pathPlayer(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
    }
    return player;
  }

  /** /cobblemon-initiative shrine <id> path record — toggle continuous recording. */
  private static int shrinePathRecord(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().toggleRecording(player, shrineId);
    return 1;
  }

  /** /cobblemon-initiative shrine <id> path here — add the block underfoot. */
  private static int shrinePathHere(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().recordHere(player, shrineId);
    return 1;
  }

  /** /cobblemon-initiative shrine <id> path clear — wipe the recorded path. */
  private static int shrinePathClear(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().clearPath(player, shrineId);
    return 1;
  }

  /** /cobblemon-initiative shrine <id> path show — particle-highlight safe blocks. */
  private static int shrinePathShow(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().showPath(player, shrineId);
    return 1;
  }

  /** /cobblemon-initiative shrine <id> path export — print a safePositions snippet. */
  private static int shrinePathExport(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = pathPlayer(context);
    if (player == null) return 0;
    String shrineId = StringArgumentType.getString(context, "shrine");
    InitiativeInit.getShrineChallengeManager().exportPath(player, shrineId);
    return 1;
  }

  // ── Reload & dev/testing helpers ──────────────────────────────────────────────

  /**
   * /cobblemon-initiative reload — re-read trainers, level caps, shrine challenges,
   * and all runtime configs without a relaunch. NOTE: reads BUILT resources — in the
   * dev client run `gradle processResources` first; a packaged jar needs a rebuild.
   */
  private static int reloadAll(CommandContext<CommandSourceStack> context) {
    InitiativeInit.getConfigLoader().loadAllConfigs();
    InitiativeInit.getShrineChallengeManager().loadChallenges();
    NuzlockeInit.reloadConfig();
    NpcSightInit.reloadConfig();
    ShrineConfig.reload();
    LootChestConfig.reload();
    ProgressionConfig.reload();
    com.thecompanyinc.cobblemoninitiative.noble.NobleEncounterInit.getManager().loadNobles();
    com.thecompanyinc.cobblemoninitiative.config.NobleConfig.reload();
    InitiativeInit.getDaycareManager().reloadConfig();
    InitiativeInit.getHomesteadManager().reloadConfig();
    InitiativeInit.getMomCareManager().reloadConfig();
    InitiativeInit.reloadFlavorConfig(context.getSource().getServer());
    context.getSource().sendSuccess(
      () ->
        Component.literal(
          "§aReloaded trainers, level caps, shrine challenges, and all configs. " +
          "§7(Reads built resources — run `gradle processResources` in the dev client, or rebuild for a packaged jar.)"
        ),
      true
    );
    return 1;
  }

  /**
   * Dishonorable Respawn (PokeballDeathScreen button, showrunner 2026-07-17). Brings a
   * dead hardcore player back in SURVIVAL and brands them: hardcore stays ARMED (the next
   * death is final again), a permanent {@code dishonored} tag is set, and a
   * {@code dishonorable_respawns} score is incremented. The trick: vanilla
   * {@code PlayerList.respawn} forces SPECTATOR when the world is hardcore, so we flip the
   * hardcore flag OFF across the respawn call, then restore it — the same live
   * {@link LevelSettingsAccessor} mutation the install command uses to promote a world.
   */
  private static int dishonoredRespawn(CommandContext<CommandSourceStack> context) {
    ServerPlayer dead = context.getSource().getPlayer();
    if (dead == null) return 0;
    net.minecraft.server.MinecraftServer server = dead.getServer();
    if (server == null) return 0;

    net.minecraft.world.level.storage.WorldData worldData = server.getWorldData();
    net.minecraft.world.level.LevelSettings settings =
      (worldData instanceof net.minecraft.world.level.storage.PrimaryLevelData pld)
        ? ((com.thecompanyinc.cobblemoninitiative.mixin.PrimaryLevelDataAccessor) (Object) pld).getSettings()
        : null;
    boolean wasHardcore = settings != null && settings.hardcore();

    if (wasHardcore) {
      ((com.thecompanyinc.cobblemoninitiative.mixin.LevelSettingsAccessor) (Object) settings).setHardcore(false);
    }
    ServerPlayer revived;
    try {
      revived = server.getPlayerList().respawn(
        dead, false, net.minecraft.world.entity.Entity.RemovalReason.KILLED);
    } finally {
      if (wasHardcore) {
        ((com.thecompanyinc.cobblemoninitiative.mixin.LevelSettingsAccessor) (Object) settings).setHardcore(true);
      }
    }

    // Back on their feet, and marked for it.
    revived.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
    revived.setHealth(revived.getMaxHealth());
    revived.getFoodData().setFoodLevel(20);
    revived.addTag("dishonored");
    net.minecraft.world.scores.Scoreboard board = server.getScoreboard();
    net.minecraft.world.scores.Objective obj =
      board.getObjective("dishonorable_respawns");
    if (obj == null) {
      obj = board.addObjective(
        "dishonorable_respawns",
        net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY,
        Component.literal("Dishonorable Respawns"),
        net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER,
        true, null);
    }
    net.minecraft.world.scores.ScoreAccess score =
      board.getOrCreatePlayerScore(revived, obj);
    score.set(score.get() + 1);

    revived.sendSystemMessage(Component.literal(
      "§8You claw back from the dark. That was not how it was meant to end — and the ledger remembers."));
    revived.level().playSound(
      null, revived.blockPosition(),
      net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
      net.minecraft.sounds.SoundSource.MASTER, 0.4f, 0.6f);
    return 1;
  }
}

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
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.LootChestConfig;
import com.thecompanyinc.cobblemoninitiative.config.ProgressionConfig;
import com.thecompanyinc.cobblemoninitiative.config.ShrineConfig;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import com.thecompanyinc.cobblemoninitiative.economy.ShopTierManager;
import com.thecompanyinc.cobblemoninitiative.npcsight.NpcSightInit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        // Verified party give (Cobblemon API): creates <species> at <level> and adds it to the
        // party, optionally tagging on success. The reliable alternative to the unverified
        // givepokemonother. /cobblemon-initiative givemon <species> <level> [tag]
        .then(
          Commands.literal("givemon")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("species", StringArgumentType.word())
                .then(
                  Commands.argument("level", IntegerArgumentType.integer(1, 100))
                    .executes(ctx -> givemon(
                      ctx,
                      StringArgumentType.getString(ctx, "species"),
                      IntegerArgumentType.getInteger(ctx, "level"),
                      null
                    ))
                    .then(
                      Commands.argument("tag", StringArgumentType.word())
                        .executes(ctx -> givemon(
                          ctx,
                          StringArgumentType.getString(ctx, "species"),
                          IntegerArgumentType.getInteger(ctx, "level"),
                          StringArgumentType.getString(ctx, "tag")
                        ))
                    )
                )
            )
        )
        .then(
          Commands.literal("track")
            .requires(source -> source.getEntity() instanceof ServerPlayer)
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
          Commands.literal("dev")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.literal("goto").then(
                Commands.argument("trainer", StringArgumentType.word())
                  .suggests((context, builder) ->
                    SharedSuggestionProvider.suggest(trainerIdsWithCoords(), builder)
                  )
                  .executes(CobblemonInitiativeCommands::devGoto)
              )
            )
            .then(
              Commands.literal("badges").then(
                Commands.argument("count", IntegerArgumentType.integer(0, 10))
                  .executes(CobblemonInitiativeCommands::devBadges)
              )
            )
            .then(
              Commands.literal("grant").then(
                Commands.argument("achievement", StringArgumentType.word())
                  .suggests((context, builder) ->
                    SharedSuggestionProvider.suggest(badgeAchievementIds(), builder)
                  )
                  .executes(CobblemonInitiativeCommands::devGrant)
              )
            )
            .then(
              Commands.literal("kit").executes(CobblemonInitiativeCommands::devKit)
            )
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
   * Verified party give (Cobblemon API). Creates {@code species} at {@code level}, adds it to
   * the player's party, and (when non-null) adds {@code successTag}. The reliable alternative
   * to the unverified {@code givepokemonother} command for quest gifts.
   */
  private static int givemon(
    CommandContext<CommandSourceStack> context,
    String species,
    int level,
    String successTag
  ) {
    ServerPlayer player;
    try {
      player = context.getSource().getPlayerOrException();
    } catch (Exception e) {
      return 0;
    }

    Pokemon given = giveSpecies(player, species, level);
    if (given == null) {
      player.sendSystemMessage(Component.literal("§cCould not create Pokémon: " + species));
      return 0;
    }
    if (successTag != null && !successTag.isBlank()) {
      player.addTag(successTag);
    }
    player.sendSystemMessage(
      Component.literal("§a" + species + " (Lv " + level + ") joined your team.")
    );
    return 1;
  }

  /** Create {@code species} at {@code level} via the Cobblemon API and add it to the party. */
  private static Pokemon giveSpecies(ServerPlayer player, String species, int level) {
    try {
      Pokemon mon = PokemonProperties.Companion
        .parse(species + " level=" + level)
        .create();
      Cobblemon.INSTANCE.getStorage().getParty(player).add(mon);
      return mon;
    } catch (Exception e) {
      return null;
    }
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
    progress.setCurrentLevelCap(20);

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

  private static List<String> trainerIdsWithCoords() {
    List<String> ids = new ArrayList<>();
    for (TrainerConfig t : InitiativeInit.getConfigLoader().getAllTrainers()) {
      int[] c = t.getCoordinates();
      if (c != null && c.length >= 3) ids.add(t.getId());
    }
    return ids;
  }

  private static List<String> badgeAchievementIds() {
    List<String> ids = new ArrayList<>();
    for (LevelCapConfig cap : InitiativeInit.getConfigLoader().getLevelCaps()) {
      if (cap.getAchievementId() != null) ids.add(cap.getAchievementId());
    }
    return ids;
  }

  /** /cobblemon-initiative dev goto &lt;trainer&gt; — teleport to a trainer's coordinates. */
  private static int devGoto(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String id = StringArgumentType.getString(context, "trainer");
    TrainerConfig t = InitiativeInit.getConfigLoader().getTrainer(id);
    if (t == null || t.getCoordinates() == null || t.getCoordinates().length < 3) {
      context.getSource().sendFailure(
        Component.literal("§cNo coordinates for trainer: " + id)
      );
      return 0;
    }
    int[] c = t.getCoordinates();
    player.connection.teleport(
      c[0] + 0.5, c[1], c[2] + 0.5, player.getYRot(), player.getXRot()
    );
    context.getSource().sendSuccess(
      () ->
        Component.literal(
          "§aTeleported to §e" + id + "§a (" + c[0] + " " + c[1] + " " + c[2] + ")."
        ),
      true
    );
    return 1;
  }

  /** /cobblemon-initiative dev badges &lt;n&gt; — set progression to exactly N gym badges. */
  private static int devBadges(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    int n = IntegerArgumentType.getInteger(context, "count");

    var configLoader = InitiativeInit.getConfigLoader();
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);

    // Badge achievements in gym order.
    List<String> badgeAchievements = new ArrayList<>();
    for (LevelCapConfig cap : configLoader.getLevelCaps()) {
      String a = cap.getAchievementId();
      if (a != null && a.startsWith("badge_")) badgeAchievements.add(a);
    }

    // Reset badge state, then grant the first N (achievements + their gym leaders).
    progress.getEarnedAchievements().removeIf(a -> a.startsWith("badge_"));
    Set<String> grant = new HashSet<>(
      badgeAchievements.subList(0, Math.min(n, badgeAchievements.size()))
    );
    for (TrainerConfig t : configLoader.getAllTrainers()) {
      String a = t.getAchievementOnDefeat();
      if (a == null || !a.startsWith("badge_")) continue;
      if (grant.contains(a)) progress.addDefeatedTrainer(t.getId());
      else progress.getDefeatedTrainers().remove(t.getId());
    }
    for (String a : grant) progress.addAchievement(a);

    InitiativeInit.getLevelCapManager().updateLevelCap(player);
    if (player.getServer() != null) {
      InitiativeInit.getProgressManager().saveProgress(player.getServer());
    }

    int cap = InitiativeInit.getLevelCapManager().getLevelCap(player);
    context.getSource().sendSuccess(
      () ->
        Component.literal(
          "§aSet progression to §e" + n + "§a badge(s); level cap now §e" + cap + "§a."
        ),
      true
    );
    return 1;
  }

  /** /cobblemon-initiative dev grant &lt;achievement&gt; — grant one achievement + refresh cap. */
  private static int devGrant(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    String achievement = StringArgumentType.getString(context, "achievement");
    PlayerProgress progress = InitiativeInit.getProgressManager().getProgress(player);
    progress.addAchievement(achievement);
    InitiativeInit.getLevelCapManager().updateLevelCap(player);
    if (player.getServer() != null) {
      InitiativeInit.getProgressManager().saveProgress(player.getServer());
    }
    context.getSource().sendSuccess(
      () -> Component.literal("§aGranted achievement §e" + achievement + "§a."),
      true
    );
    return 1;
  }

  /** /cobblemon-initiative dev kit — give the shrine crystals + test items. */
  private static int devKit(CommandContext<CommandSourceStack> context) {
    ServerPlayer player = context.getSource().getPlayer();
    if (player == null) {
      context.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    var server = player.getServer();
    if (server == null) return 0;

    String name = player.getName().getString();
    String[] gives = {
      "cobblemon-initiative:fire_shrine_crystal",
      "cobblemon-initiative:ground_shrine_crystal",
      "cobblemon-initiative:ice_shrine_crystal",
      "cobblemon-initiative:dragon_shrine_crystal",
      "cobblemon-initiative:fairy_shrine_crystal",
      "cobblemon:rare_candy 16",
      "cobblemon:potion 16",
    };
    var source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
    for (String give : gives) {
      server.getCommands().performPrefixedCommand(source, "give " + name + " " + give);
    }
    context.getSource().sendSuccess(
      () -> Component.literal("§aDev kit granted: 5 shrine crystals + rare candy + potions."),
      true
    );
    return 1;
  }
}

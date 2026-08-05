package com.thecompanyinc.cobblemoninitiative.devtools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.LevelCapConfig;
import com.thecompanyinc.cobblemoninitiative.config.TrainerConfig;
import com.thecompanyinc.cobblemoninitiative.data.PlayerProgress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * The {@code /ca-dev …} subtree — every dev-only command in one registration under the
 * dedicated top-level {@code ca-dev} root (strips with the devtools package at 1.0.0, TODO §2).
 * Brigadier merges the separate {@code ca-dev} registrations from the other devtools files
 * (DevBotCommand, DevNoteCommand, GymMarkCommand, TestCommands) into one tree; keeping the whole
 * dev surface off the shipping {@code cobblemon-initiative}/{@code /ca} tree makes it a single
 * strip target on release.
 *
 * <p>Handlers for badges/grant moved verbatim from CobblemonInitiativeCommands
 * (2026-07-11 consolidation); stage/place delegate to
 * {@link DevTestManager}/{@link DevPlaceManager}. (goto/tool/path/kit/showdown/team pruned
 * 2026-07-30 — unused in playtest.)
 */
public final class DevCommands {

  private DevCommands() {}

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
      Commands.literal("ca-dev")
        .requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("badges").then(
              Commands.argument("count", IntegerArgumentType.integer(0, 10))
                .executes(DevCommands::devBadges)
            )
          )
          .then(
            Commands.literal("grant").then(
              Commands.argument("achievement", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(badgeAchievementIds(), builder)
                )
                .executes(DevCommands::devGrant)
            )
          )
          // Full-heal the party + self, in or out of battle (playtest convenience).
          .then(
            Commands.literal("heal").executes(ctx -> withPlayer(ctx, DevCommands::devHeal))
          )
          // Outline every Easy NPC so the producer can find placed bodies at a glance.
          .then(
            Commands.literal("glow")
              .executes(ctx -> devGlow(ctx, true))
              .then(
                Commands.argument("state", StringArgumentType.word())
                  .suggests((c, b) -> SharedSuggestionProvider.suggest(new String[] {"on", "off"}, b))
                  .executes(ctx ->
                    devGlow(ctx, !"off".equalsIgnoreCase(StringArgumentType.getString(ctx, "state"))))
              )
          )
          // Preview any PokePhone call-screen script on demand (preview ring — full UX,
          // completion skips side effects, so the real story trigger stays intact).
          .then(
            Commands.literal("phone").then(
              Commands.argument("call", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                  InitiativeInit.getPhoneCallManager().scriptIds(), b))
                .executes(ctx ->
                  withPlayer(ctx, p -> devPhone(p, StringArgumentType.getString(ctx, "call"))))
            )
          )
          .then(
            Commands.literal("stage").then(
              Commands.argument("stage", StringArgumentType.word())
                .suggests((context, builder) ->
                  SharedSuggestionProvider.suggest(DevTestManager.stageIds(), builder)
                )
                .executes(ctx -> withPlayer(ctx, p ->
                  DevTestManager.applyStage(p, StringArgumentType.getString(ctx, "stage"))))
            )
          )
          // Guided placement walk: tp through proposals, record placements/adoptions.
          .then(
            Commands.literal("place")
              .then(Commands.literal("list").executes(ctx -> withPlayer(ctx, DevPlaceManager::cmdList)))
              .then(Commands.literal("next").executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdNext(p, true))))
              .then(Commands.literal("prev").executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdNext(p, false))))
              .then(Commands.literal("export").executes(ctx -> withPlayer(ctx, DevPlaceManager::cmdExport)))
              .then(Commands.literal("goto").then(placeIdArg().executes(
                ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdGoto(p, StringArgumentType.getString(ctx, "id"))))))
              .then(Commands.literal("here")
                .executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdHere(p, null)))
                .then(placeIdArg().executes(
                  ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdHere(p, StringArgumentType.getString(ctx, "id"))))))
              .then(Commands.literal("adopt")
                .executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdAdopt(p, null)))
                .then(placeIdArg().executes(
                  ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdAdopt(p, StringArgumentType.getString(ctx, "id"))))))
              .then(Commands.literal("skip")
                .executes(ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdSkip(p, null)))
                .then(placeIdArg().executes(
                  ctx -> withPlayer(ctx, p -> DevPlaceManager.cmdSkip(p, StringArgumentType.getString(ctx, "id"))))))
          )
          // Quick freeform playtest note: /ca-dev note <text>.
          .then(
            Commands.literal("note").then(
              Commands.argument("text", StringArgumentType.greedyString())
                .executes(ctx -> withPlayer(ctx, p -> {
                  DevNoteInit.addFreeNote(p, StringArgumentType.getString(ctx, "text"));
                  return 1;
                }))
            )
          )
          // Block-marker tool: give it (no arg), or manage the live selection.
          .then(
            Commands.literal("marker")
              .executes(ctx -> withPlayer(ctx, p -> { DevMarkerManager.giveTool(p); return 1; }))
              .then(Commands.literal("give")
                .executes(ctx -> withPlayer(ctx, p -> { DevMarkerManager.giveTool(p); return 1; })))
              .then(Commands.literal("status")
                .executes(ctx -> withPlayer(ctx, p -> { DevMarkerManager.status(p); return 1; })))
              .then(Commands.literal("undo")
                .executes(ctx -> withPlayer(ctx, p -> { DevMarkerManager.undo(p); return 1; })))
              .then(Commands.literal("clear")
                .executes(ctx -> withPlayer(ctx, p -> { DevMarkerManager.clear(p); return 1; })))
              .then(Commands.literal("save").then(
                Commands.argument("title", StringArgumentType.string())
                  .executes(ctx -> withPlayer(ctx, p ->
                    DevMarkerManager.save(p, StringArgumentType.getString(ctx, "title"), null) ? 1 : 0))
                  .then(Commands.argument("description", StringArgumentType.greedyString())
                    .executes(ctx -> withPlayer(ctx, p -> DevMarkerManager.save(p,
                      StringArgumentType.getString(ctx, "title"),
                      StringArgumentType.getString(ctx, "description")) ? 1 : 0)))
              ))
          )
          // Aggregate dev log -> chat + a markdown file next to the save (upload it for notes).
          .then(
            Commands.literal("log").executes(ctx -> withPlayer(ctx, DevNoteInit::devLog))
          )
    );
  }

  // ---------------------------------------------------------------------------
  // Handlers (moved verbatim from CobblemonInitiativeCommands)
  // ---------------------------------------------------------------------------

  private static List<String> badgeAchievementIds() {
    List<String> ids = new ArrayList<>();
    for (LevelCapConfig cap : InitiativeInit.getConfigLoader().getLevelCaps()) {
      if (cap.getAchievementId() != null) ids.add(cap.getAchievementId());
    }
    return ids;
  }

  /** /ca-dev badges &lt;n&gt; — set progression to exactly N gym badges. */
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
      // Also toggle the defeated_<leader> PLAYER TAG that the real TBCS onwin sets. Dialog
      // and quest gates lower to this tag (content_compile: defeated → player_tag
      // defeated_<id>), NOT to the progress Set — so without this, dev-badged players fail
      // every gym-progression dialog/quest stage and late-game content isn't testable.
      // PlayerProgressManager treats the tag and the Set as equivalent, so no double-count.
      if (grant.contains(a)) {
        progress.addDefeatedTrainer(t.getId());
        player.addTag("defeated_" + t.getId());
      } else {
        progress.getDefeatedTrainers().remove(t.getId());
        player.removeTag("defeated_" + t.getId());
      }
    }
    for (String a : grant) progress.addAchievement(a);

    // Mirror the memory_fragment score band_tags derives badges_gte_N from — without
    // this, dev-granted badges never light the recognition tiers (live-caught by the
    // memo_checkpoint scenario authoring, 2026-07-18).
    if (player.getServer() != null) {
      var scoreboard = player.getServer().getScoreboard();
      var fragObj = scoreboard.getObjective("memory_fragment");
      if (fragObj != null) {
        scoreboard.getOrCreatePlayerScore(player, fragObj).set(n);
      }
    }

    InitiativeInit.getLevelCapManager().updateLevelCap(player);
    if (player.getServer() != null) {
      InitiativeInit.getProgressManager().saveProgress(player.getServer());
    }

    int cap = InitiativeInit.getLevelCapManager().getLevelCap(player);
    context.getSource().sendSuccess(
      () ->
        Component.literal(
          "§aSet progression to §e" + n + "§a badge(s) + defeated tags; level cap now §e"
            + cap + "§a. §7(endgame story flags: /ca-dev stage <era>)"
        ),
      true
    );

    // Playtest trap (alpha.2, route-3 trio): every forced VERY_CLOSE ambush battle is
    // gated on the dex_gte_2 fairness floor, and a fresh dev-badged profile usually has
    // caught nothing — the ambush silently never fires (and Easy NPC's per-band
    // de-dupe means it can't re-fire while the pursuer pins you inside the band).
    // Warn instead of faking dex entries: the floor is a B1 fairness rule, not a gate
    // to bypass.
    if (player.getServer() != null) {
      var scoreboard = player.getServer().getScoreboard();
      var dexObj = scoreboard.getObjective("dex_caught");
      long dexCaught = dexObj != null
        ? scoreboard.getOrCreatePlayerScore(player, dexObj).get()
        : 0;
      if (dexCaught < 2) {
        context.getSource().sendSuccess(
          () ->
            Component.literal(
              "§6⚠ dex_caught < 2 — forced route ambushes (dex_gte_2 fairness floor) "
                + "will NOT trigger on this profile. Catch two Pokémon to test them."
            ),
          false
        );
      }
    }
    return 1;
  }

  /** /ca-dev grant &lt;achievement&gt; — grant one achievement + refresh cap. */
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

  /**
   * /ca-dev heal — fully heal the player's whole party AND the player
   * (health, hunger, fire, effects), usable mid-battle. Party uses the shipped healParty
   * idiom; the {@code healpokemon} console command resyncs a currently sent-out mon into
   * the running showdown side (the FrontierManager pattern), avoiding fragile BattlePokemon
   * surgery. Player self-heal is plain vanilla.
   */
  private static int devHeal(ServerPlayer player) {
    var server = player.getServer();
    try {
      var party = com.cobblemon.mod.common.Cobblemon.INSTANCE.getStorage().getParty(player);
      for (int i = 0; i < party.size(); i++) {
        var mon = party.get(i);
        if (mon != null) mon.heal();
      }
    } catch (Exception ignored) {
      // storage lookup can throw for a party-less player; the party heal is best-effort.
    }
    if (server != null) {
      server.getCommands().performPrefixedCommand(
        server.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
        "healpokemon " + player.getGameProfile().getName());
    }
    player.setHealth(player.getMaxHealth());
    player.getFoodData().setFoodLevel(20);
    player.getFoodData().setSaturation(20.0f);
    player.clearFire();
    player.setAirSupply(player.getMaxAirSupply());
    player.removeAllEffects();
    player.sendSystemMessage(Component.literal("§a✚ Party and self fully healed."));
    return 1;
  }

  /**
   * /ca-dev glow [on|off] — toggle the vanilla GLOWING outline on every
   * Easy NPC entity across all levels (namespace {@code easy_npc}: humanoid + cobblemon_npc
   * + fairy). One-shot sweep — re-run after chunks load elsewhere. Defaults to on.
   */
  private static int devGlow(CommandContext<CommandSourceStack> context, boolean on) {
    var server = context.getSource().getServer();
    int count = 0;
    for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
      for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
        if (e instanceof ServerPlayer) continue;
        var key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
        if (!"easy_npc".equals(key.getNamespace())) continue;
        e.setGlowingTag(on);
        count++;
      }
    }
    final int n = count;
    context.getSource().sendSuccess(
      () -> Component.literal(
        "§a" + (on ? "Glowing " : "Cleared glow on ") + "§e" + n + "§a Easy NPC entities."),
      true
    );
    return count;
  }

  /**
   * /ca-dev phone &lt;call&gt; — preview a PokePhone call-screen script (0.7.0-alpha.20)
   * on demand: {@code PhoneCallManager.ringPreview} runs the full ring/answer/pages/choices
   * UX, but completion skips choice commands, on_complete, and the done_tag grant, so the
   * real condition-driven call still rings later in normal play.
   */
  private static int devPhone(ServerPlayer player, String call) {
    var manager = InitiativeInit.getPhoneCallManager();
    return switch (manager.ringPreview(player, call)) {
      case QUEUED -> {
        player.sendSystemMessage(Component.literal(
          "§a☎ Preview of §e" + call + "§a queued to ring — side effects will be skipped."));
        yield 1;
      }
      case ALREADY_PENDING -> {
        player.sendSystemMessage(Component.literal(
          "§7☎ '" + call + "' is already ringing/queued for you."));
        yield 0;
      }
      case UNKNOWN_ID -> {
        player.sendSystemMessage(Component.literal(
          "§cUnknown call '" + call + "'. Options: "
            + String.join(", ", manager.scriptIds())));
        yield 0;
      }
    };
  }

  // ---------------------------------------------------------------------------
  // Plumbing
  // ---------------------------------------------------------------------------

  /** Plan-id argument with suggestions from the bundled placement plan. */
  private static RequiredArgumentBuilder<CommandSourceStack, String> placeIdArg() {
    return Commands.argument("id", StringArgumentType.word())
      .suggests((context, builder) ->
        SharedSuggestionProvider.suggest(
          DevPlaceManager.planIds(context.getSource().getServer()), builder)
      );
  }

  /** Run a player-only dev handler, failing cleanly from a non-player source. */
  private static int withPlayer(
    CommandContext<CommandSourceStack> ctx, java.util.function.ToIntFunction<ServerPlayer> fn
  ) {
    ServerPlayer p = ctx.getSource().getPlayer();
    if (p == null) {
      ctx.getSource().sendFailure(Component.literal("Must be run by a player."));
      return 0;
    }
    return fn.applyAsInt(p);
  }

}

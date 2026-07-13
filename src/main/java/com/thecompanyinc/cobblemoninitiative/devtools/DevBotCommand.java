package com.thecompanyinc.cobblemoninitiative.devtools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * {@code /cobblemon-initiative dev bot …} — synthetic player interactions for headless
 * (Carpet fake-player) testing. Carpet's own {@code player X use} silently NO-OPS for
 * block/item interaction, so bots cannot right-click blocks, open dialogs, or throw
 * Pokéballs — these subcommands go through the REAL server-side interaction entry points
 * instead, so every Fabric callback and mod hook fires exactly as for a human click.
 *
 * <p>Like the {@code track} subtree (and {@code givemon}), the acting player is resolved
 * at RUNTIME from the command SOURCE — no parse-time entity argument — so
 * {@code execute as <bot> run cobblemon-initiative dev bot …} drives any bot. Use
 * {@code execute as <bot> at @s run …} when passing relative coordinates.
 *
 * <p>Verified against the loom-remapped jars (mojmap):
 * <ul>
 *   <li>{@code use} → {@code ServerPlayerGameMode.useItemOn(ServerPlayer, Level,
 *       ItemStack, InteractionHand, BlockHitResult)} — Fabric's
 *       {@code UseBlockCallback.EVENT} fires inside it
 *       (fabric-events-interaction-v0 {@code ServerPlayerInteractionManagerMixin},
 *       bytecode-verified), so lootchest/docprop/install hooks all see the click.</li>
 *   <li>{@code useitem} → {@code ServerPlayerGameMode.useItem(…)} — the Pokéball throw
 *       path ({@code PokeBallItem.use} throws along the player's look vector; aim
 *       first).</li>
 *   <li>{@code aim} → {@code ServerPlayer.lookAt(Anchor.EYES, …)} — sets server-side
 *       yaw/pitch/head-rot ({@code Entity.lookAt}) and syncs a real client via
 *       {@code ClientboundPlayerLookAtPacket} (harmlessly swallowed for fake
 *       players).</li>
 *   <li>{@code interact} → {@code Player.interactOn(Entity, InteractionHand)} — opens
 *       Easy NPC dialogs server-side (the dialog SCREEN is a packet to the client, but
 *       the open action + ON_OPEN triggers fire).</li>
 * </ul>
 *
 * <p>Every result is reported as a plain {@code [BOT] <subcmd> … result=<…>} line
 * (RCON-readable). Dev-only: strips with the devtools package at 1.0.0 (TODO §2).
 */
public final class DevBotCommand {

  private DevBotCommand() {}

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
      Commands.literal("cobblemon-initiative").then(
        // requires() repeated here so the perm-2 gate survives any brigadier merge
        // order with DevCommands' "dev" literal (merge keeps the FIRST node's gate).
        Commands.literal("dev")
          .requires(source -> source.hasPermission(2))
          .then(
            Commands.literal("bot")
              .requires(source -> source.hasPermission(2))
              // dev bot use <x> <y> <z> [face] — synthetic right-click-on-block.
              .then(
                Commands.literal("use").then(
                  Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(ctx -> use(ctx, null))
                    .then(
                      Commands.argument("face", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                          java.util.Arrays.stream(Direction.values())
                            .map(Direction::getSerializedName),
                          builder
                        ))
                        .executes(ctx -> use(ctx, StringArgumentType.getString(ctx, "face")))
                    )
                )
              )
              // dev bot useitem — synthetic right-click-air (Pokéball throw path).
              .then(Commands.literal("useitem").executes(DevBotCommand::useItem))
              // dev bot aim <x> <y> <z> | aim entity <target> — face the target.
              .then(
                Commands.literal("aim")
                  .then(
                    Commands.literal("entity").then(
                      Commands.argument("target", EntityArgument.entity())
                        .executes(DevBotCommand::aimEntity)
                    )
                  )
                  .then(
                    Commands.argument("pos", Vec3Argument.vec3())
                      .executes(DevBotCommand::aimPos)
                  )
              )
              // dev bot interact <entity> — synthetic entity right-click.
              .then(
                Commands.literal("interact").then(
                  Commands.argument("target", EntityArgument.entity())
                    .executes(DevBotCommand::interact)
                )
              )
              // dev bot autobattle on|off — toggle the auto-battler for the source.
              .then(
                Commands.literal("autobattle")
                  .then(Commands.literal("on").executes(ctx -> autobattle(ctx, true)))
                  .then(Commands.literal("off").executes(ctx -> autobattle(ctx, false)))
              )
          )
      )
    );
  }

  // ---------------------------------------------------------------------------
  // Handlers — all act on the SOURCE player (execute-as pattern).

  private static int use(CommandContext<CommandSourceStack> ctx, String faceName)
      throws CommandSyntaxException {
    ServerPlayer player = requirePlayer(ctx);
    if (player == null) return 0;

    Direction face = Direction.UP;
    if (faceName != null) {
      face = Direction.byName(faceName);
      if (face == null) {
        ctx.getSource().sendFailure(
          Component.literal("[BOT] use result=BAD_FACE (" + faceName + " — use up/down/north/south/east/west)")
        );
        return 0;
      }
    }

    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
    ItemStack held = player.getMainHandItem();
    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false);
    // Real server-side entry point — Fabric's UseBlockCallback fires inside this call.
    InteractionResult result = player.gameMode.useItemOn(
      player, player.serverLevel(), held, InteractionHand.MAIN_HAND, hit
    );

    String line = "[BOT] use " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
      + " face=" + face.getSerializedName() + " item=" + itemId(held) + " result=" + result;
    ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    return result.consumesAction() ? 1 : 0;
  }

  private static int useItem(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = requirePlayer(ctx);
    if (player == null) return 0;

    ItemStack held = player.getMainHandItem();
    // Right-click-air path: PokeBallItem.use throws along the player's look vector.
    InteractionResult result = player.gameMode.useItem(
      player, player.serverLevel(), held, InteractionHand.MAIN_HAND
    );

    String line = "[BOT] useitem item=" + itemId(held) + " result=" + result;
    ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    return result.consumesAction() ? 1 : 0;
  }

  private static int aimPos(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = requirePlayer(ctx);
    if (player == null) return 0;

    Vec3 target = Vec3Argument.getVec3(ctx, "pos");
    // Sets server-side yaw/pitch/head-rot AND syncs a real client (packet is a no-op
    // for Carpet fake players — the server rotation is what the throw path reads).
    player.lookAt(EntityAnchorArgument.Anchor.EYES, target);

    String line = "[BOT] aim " + target.x + " " + target.y + " " + target.z
      + " result=yaw=" + String.format("%.1f", player.getYRot())
      + ",pitch=" + String.format("%.1f", player.getXRot());
    ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    return 1;
  }

  private static int aimEntity(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    ServerPlayer player = requirePlayer(ctx);
    if (player == null) return 0;

    Entity target = EntityArgument.getEntity(ctx, "target");
    player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);

    String line = "[BOT] aim entity=" + target.getName().getString()
      + " result=yaw=" + String.format("%.1f", player.getYRot())
      + ",pitch=" + String.format("%.1f", player.getXRot());
    ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    return 1;
  }

  private static int interact(CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    ServerPlayer player = requirePlayer(ctx);
    if (player == null) return 0;

    Entity target = EntityArgument.getEntity(ctx, "target");
    // Vanilla entity right-click path (interactAt + mobInteract) — Easy NPC opens its
    // dialog server-side here; the screen packet just has no client to land on.
    InteractionResult result = player.interactOn(target, InteractionHand.MAIN_HAND);

    String line = "[BOT] interact target=" + target.getName().getString()
      + " (" + target.getType().getDescription().getString() + ") result=" + result;
    ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    return result.consumesAction() ? 1 : 0;
  }

  private static int autobattle(CommandContext<CommandSourceStack> ctx, boolean on) {
    ServerPlayer player = requirePlayer(ctx);
    if (player == null) return 0;

    boolean changed = AutoBattler.setEnrolled(player, on);
    String line = "[BOT] autobattle result=" + (on ? "on" : "off")
      + (changed ? "" : " (already)") + " player=" + player.getGameProfile().getName();
    ctx.getSource().sendSuccess(() -> Component.literal(line), false);
    return 1;
  }

  // ---------------------------------------------------------------------------

  /** Resolve the acting player from the SOURCE at runtime (execute-as pattern). */
  private static ServerPlayer requirePlayer(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer p = ctx.getSource().getPlayer();
    if (p == null) {
      ctx.getSource().sendFailure(Component.literal(
        "[BOT] result=NO_PLAYER (run as a player: execute as <bot> run cobblemon-initiative dev bot …)"
      ));
    }
    return p;
  }

  private static String itemId(ItemStack stack) {
    return stack.isEmpty() ? "empty" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
  }
}

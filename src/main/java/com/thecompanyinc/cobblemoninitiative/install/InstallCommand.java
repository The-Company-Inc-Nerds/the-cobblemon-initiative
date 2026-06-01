package com.thecompanyinc.cobblemoninitiative.install;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.npcmap.NpcMapEntry;
import com.thecompanyinc.cobblemoninitiative.npcmap.NpcMapInit;
import com.thecompanyinc.cobblemoninitiative.npcmap.NpcMapStorage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * /cobblemon-initiative install check|run
 *
 * <p><b>check</b> — reports current vs. target gamerule values and how many NPC
 * preset mappings are registered.
 *
 * <p><b>run</b> — applies all gamerules from install.json, then delegates NPC preset
 * application to the existing npc-map storage (same logic as
 * {@code /cobblemon-initiative npc-map apply}).
 *
 * <p>Mod dependencies belong in {@code fabric.mod.json}; the launcher resolves them.
 * NPC position/home data lives in the Easy NPC SNBT preset files; no coordinates are
 * stored here.
 */
public class InstallCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative-install");
  private static final Gson GSON = new GsonBuilder().create();
  private static final String CONFIG_PATH = "data/cobblemon_initiative/install.json";

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher.register(
      Commands.literal("cobblemon-initiative")
        .then(
          Commands.literal("install")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("check").executes(InstallCommand::cmdCheck))
            .then(Commands.literal("run").executes(InstallCommand::cmdRun))
        )
    );
  }

  // ---------------------------------------------------------------------------
  // /cobblemon-initiative install check
  // ---------------------------------------------------------------------------

  private static int cmdCheck(CommandContext<CommandSourceStack> ctx) {
    InstallConfig config = loadConfig(ctx);
    if (config == null) return 0;

    MinecraftServer server = ctx.getSource().getServer();
    StringBuilder sb = new StringBuilder("[Install] Status:\n\n  Gamerules:\n");

    for (var entry : config.gamerules.entrySet()) {
      String rule = entry.getKey();
      String target = entry.getValue();
      if (rule.equals("_difficulty")) {
        String current = server.getWorldData().getDifficulty().getKey();
        boolean ok = current.equalsIgnoreCase(target);
        sb.append("    [").append(ok ? "OK" : "DRIFT").append("] difficulty = ")
          .append(current);
        if (!ok) sb.append(" → target: ").append(target);
        sb.append("\n");
      } else {
        sb.append("    [ -- ] ").append(rule).append(" → target: ").append(target).append("\n");
      }
    }

    NpcMapStorage storage = NpcMapInit.getStorage();
    int mapped = storage != null ? storage.size() : 0;
    sb.append("\n  NPC presets: ").append(mapped)
      .append(" mapping(s) in npc-map storage.\n")
      .append("  Run '/cobblemon-initiative npc-map apply' to apply them,\n")
      .append("  or '/function cobblemon_initiative:update_npc_presets' after running generate_npc_function.");

    String msg = sb.toString().trim();
    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
    return 1;
  }

  // ---------------------------------------------------------------------------
  // /cobblemon-initiative install run
  // ---------------------------------------------------------------------------

  private static int cmdRun(CommandContext<CommandSourceStack> ctx) {
    InstallConfig config = loadConfig(ctx);
    if (config == null) return 0;

    MinecraftServer server = ctx.getSource().getServer();
    CommandSourceStack silentOp = server.createCommandSourceStack()
      .withPermission(4)
      .withSuppressedOutput();

    // Apply gamerules / difficulty
    for (var entry : config.gamerules.entrySet()) {
      String rule = entry.getKey();
      String value = entry.getValue();
      if (rule.equals("_difficulty")) {
        applyDifficulty(server, value, ctx);
      } else {
        server.getCommands().performPrefixedCommand(silentOp, "gamerule " + rule + " " + value);
        ctx.getSource().sendSuccess(
          () -> Component.literal("[Install] gamerule " + rule + " = " + value), true
        );
      }
    }

    // Apply NPC presets via the existing npc-map storage (same as npc-map apply)
    NpcMapStorage storage = NpcMapInit.getStorage();
    if (storage == null || storage.size() == 0) {
      ctx.getSource().sendSuccess(
        () -> Component.literal(
          "[Install] No NPC preset mappings registered yet. " +
          "Use '/cobblemon-initiative npc-map add' or run generate_npc_function " +
          "then '/function cobblemon_initiative:update_npc_presets'."
        ), false
      );
    } else {
      int applied = 0;
      for (NpcMapEntry e : storage.getAll()) {
        try {
          String cmd = "easy_npc preset import data " + e.uuid + " " + e.preset;
          server.getCommands().performPrefixedCommand(silentOp, cmd);
          applied++;
        } catch (Exception ex) {
          LOGGER.warn("[Install] Failed to apply preset for {}: {}", e.uuid, ex.getMessage());
        }
      }
      final int count = applied;
      ctx.getSource().sendSuccess(
        () -> Component.literal("[Install] Applied " + count + "/" + storage.size() + " NPC preset(s)."),
        true
      );
    }

    return 1;
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static InstallConfig loadConfig(CommandContext<CommandSourceStack> ctx) {
    try (InputStream stream = InstallCommand.class.getClassLoader()
        .getResourceAsStream(CONFIG_PATH)) {
      if (stream == null) {
        ctx.getSource().sendFailure(
          Component.literal("[Install] install.json not found in mod resources.")
        );
        return null;
      }
      return GSON.fromJson(
        new InputStreamReader(stream, StandardCharsets.UTF_8),
        InstallConfig.class
      );
    } catch (Exception e) {
      ctx.getSource().sendFailure(
        Component.literal("[Install] Failed to load install.json: " + e.getMessage())
      );
      LOGGER.error("Failed to load install.json", e);
      return null;
    }
  }

  private static void applyDifficulty(MinecraftServer server, String name,
      CommandContext<CommandSourceStack> ctx) {
    Difficulty diff = switch (name.toLowerCase()) {
      case "peaceful" -> Difficulty.PEACEFUL;
      case "easy"     -> Difficulty.EASY;
      case "hard"     -> Difficulty.HARD;
      default         -> Difficulty.NORMAL;
    };
    server.setDifficulty(diff, true);
    ctx.getSource().sendSuccess(
      () -> Component.literal("[Install] difficulty = " + name), true
    );
  }
}

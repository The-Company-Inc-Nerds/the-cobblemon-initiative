package com.thecompanyinc.cobblemoninitiative.install;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.config.NuzlockeConfig;
import com.thecompanyinc.cobblemoninitiative.economy.ShopTierManager;
import com.thecompanyinc.cobblemoninitiative.mapfrontiers.MapFrontiersBridge;
import com.thecompanyinc.cobblemoninitiative.mixin.LevelSettingsAccessor;
import com.thecompanyinc.cobblemoninitiative.mixin.PrimaryLevelDataAccessor;
import com.thecompanyinc.cobblemoninitiative.npcmap.NpcMapEntry;
import com.thecompanyinc.cobblemoninitiative.npcmap.NpcMapInit;
import com.thecompanyinc.cobblemoninitiative.npcmap.NpcMapStorage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(
    "cobblemon-initiative-install"
  );
  private static final Gson GSON = new GsonBuilder().create();
  private static final String CONFIG_PATH =
    "data/cobblemon_initiative/install.json";

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher
  ) {
    dispatcher.register(
      Commands.literal("cobblemon-initiative").then(
        Commands.literal("install")
          .requires(src -> src.hasPermission(2))
          .then(Commands.literal("check").executes(InstallCommand::cmdCheck))
          .then(Commands.literal("verify").executes(InstallCommand::cmdVerify))
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
        sb.append("    [")
          .append(ok ? "OK" : "DRIFT")
          .append("] difficulty = ")
          .append(current);
        if (!ok) sb.append(" → target: ").append(target);
        sb.append("\n");
      } else {
        sb.append("    [ -- ] ")
          .append(rule)
          .append(" → target: ")
          .append(target)
          .append("\n");
      }
    }

    boolean currentHardcore = server.getWorldData().isHardcore();
    sb.append("    [")
      .append(config.hardcore == currentHardcore ? "OK" : "DRIFT")
      .append("] hardcore = ")
      .append(currentHardcore);
    if (config.hardcore != currentHardcore) sb.append(" → target: ").append(
      config.hardcore
    );
    sb.append("\n");

    NpcMapStorage storage = NpcMapInit.getStorage();
    int mapped = storage != null ? storage.size() : 0;
    sb.append("\n  NPC presets: ")
      .append(mapped)
      .append(" mapping(s) in npc-map storage.\n")
      .append("  Run '/cobblemon-initiative npc-map apply' to apply them,\n")
      .append(
        "  or '/function cobblemon_initiative:update_npc_presets' after running generate_npc_function."
      );

    int zoneCount = config.zones != null ? config.zones.size() : 0;
    sb.append("\n\n  Zones: ")
      .append(zoneCount)
      .append(" zone(s) defined in install.json.\n");
    if (zoneCount > 0) {
      for (InstallZone z : config.zones) {
        sb.append("    [").append(z.type).append("] ").append(z.name);
        if (z.hasVertices()) {
          sb.append(" polygon(")
            .append(z.vertices.size())
            .append(" pts)" + " cx=")
            .append(z.derivedCenterX())
            .append(" cz=")
            .append(z.derivedCenterZ())
            .append(" r=")
            .append(z.derivedRadius());
        } else {
          sb.append(" at ")
            .append(z.centerX)
            .append(", ")
            .append(z.centerY)
            .append(", ")
            .append(z.centerZ)
            .append(" r=")
            .append(z.radius);
        }
        if (z.announce) sb.append(" [announces]");
        sb.append("\n");
      }
      sb.append(
        "  Run 'install run' to apply as safe zones and write Map Frontiers frontier file."
      );
    }

    String msg = sb.toString().trim();
    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
    return 1;
  }

  // ---------------------------------------------------------------------------
  // /cobblemon-initiative install verify
  // ---------------------------------------------------------------------------

  /** Reports which expected mods / datapacks are present and recommends packs. */
  private static int cmdVerify(CommandContext<CommandSourceStack> ctx) {
    InstallConfig config = loadConfig(ctx);
    if (config == null) return 0;

    MinecraftServer server = ctx.getSource().getServer();
    StringBuilder sb = new StringBuilder("[Install] Modpack verification:\n\n");
    boolean allRequired = appendVerifyReport(sb, server, config);

    String msg = sb.toString().trim();
    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
    return allRequired ? 1 : 0;
  }

  /**
   * Appends the four-section modpack report to {@code sb}.
   * @return true if every {@code required} mod is present.
   */
  private static boolean appendVerifyReport(
    StringBuilder sb,
    MinecraftServer server,
    InstallConfig config
  ) {
    FabricLoader loader = FabricLoader.getInstance();

    // --- Mods (split required / optional) ---
    int reqPresent = 0, reqTotal = 0, optPresent = 0, optTotal = 0;
    StringBuilder reqSb = new StringBuilder();
    StringBuilder optSb = new StringBuilder();

    for (InstallConfig.ExpectedMod m : config.expectedMods) {
      ModContainer found = resolveMod(loader, m);
      boolean present = found != null;
      String label = (m.name != null && !m.name.isBlank()) ? m.name : m.modId;
      StringBuilder target = m.required ? reqSb : optSb;
      target.append("    [").append(present ? "OK" : "MISSING").append("] ").append(label);
      if (present) {
        target.append("  ").append(found.getMetadata().getVersion().getFriendlyString());
      } else if (m.note != null && !m.note.isBlank()) {
        target.append("  — ").append(m.note);
      }
      target.append("\n");
      if (m.required) { reqTotal++; if (present) reqPresent++; }
      else { optTotal++; if (present) optPresent++; }
    }

    sb.append("  Mods (required):\n").append(reqTotal == 0 ? "    (none listed)\n" : reqSb);
    sb.append("  Mods (optional / integrations):\n").append(optTotal == 0 ? "    (none listed)\n" : optSb);

    // --- Datapacks ---
    sb.append("\n  Datapacks:\n");
    if (config.expectedDatapacks.isEmpty()) {
      sb.append("    (none listed)\n");
    } else {
      PackRepository repo = server.getPackRepository();
      Collection<String> selected = repo.getSelectedIds();
      Collection<Pack> available = repo.getAvailablePacks();
      for (String want : config.expectedDatapacks) {
        String needle = want.toLowerCase();
        Pack match = null;
        for (Pack p : available) {
          if (p.getId().toLowerCase().contains(needle)) { match = p; break; }
        }
        if (match == null) {
          sb.append("    [MISSING] ").append(want).append("\n");
        } else if (selected.contains(match.getId())) {
          sb.append("    [OK] ").append(match.getId()).append(" (enabled)\n");
        } else {
          sb.append("    [DISABLED] ").append(match.getId()).append(" (available, not selected)\n");
        }
      }
    }

    // --- Resource packs (recommend only; client packs aren't read server-side) ---
    sb.append("\n  Resource packs:\n");
    sb.append("    [SHIPPED] trainer_textures (bundled with this mod, default-enabled)\n");
    for (String rp : config.recommendedResourcePacks) {
      sb.append("    recommend: ").append(rp).append(" (not verified — client-side)\n");
    }

    // --- Shaders (loader presence + recommend) ---
    sb.append("\n  Shaders (optional, cosmetic):\n");
    boolean iris = loader.isModLoaded("iris");
    boolean sodium = loader.isModLoaded("sodium");
    sb.append("    [").append(iris ? "OK" : "--").append("] Iris ")
      .append(iris ? "installed" : "not installed").append("\n");
    sb.append("    [").append(sodium ? "OK" : "--").append("] Sodium ")
      .append(sodium ? "installed" : "not installed").append("\n");
    for (String s : config.recommendedShaders) {
      sb.append("    recommend: ").append(s).append(" (selection not verified)\n");
    }

    // --- Summary ---
    sb.append("\n  Summary: ")
      .append(reqPresent).append("/").append(reqTotal).append(" required present, ")
      .append(optPresent).append("/").append(optTotal).append(" optional present.");

    return reqPresent == reqTotal;
  }

  /** Resolve a mod by its id or any alias; null if none is loaded. */
  private static ModContainer resolveMod(FabricLoader loader, InstallConfig.ExpectedMod m) {
    if (m.modId != null) {
      Optional<ModContainer> c = loader.getModContainer(m.modId);
      if (c.isPresent()) return c.get();
    }
    if (m.aliases != null) {
      for (String alias : m.aliases) {
        Optional<ModContainer> c = loader.getModContainer(alias);
        if (c.isPresent()) return c.get();
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------
  // /cobblemon-initiative install run
  // ---------------------------------------------------------------------------

  private static int cmdRun(CommandContext<CommandSourceStack> ctx) {
    InstallConfig config = loadConfig(ctx);
    if (config == null) return 0;

    MinecraftServer server = ctx.getSource().getServer();
    CommandSourceStack silentOp = server
      .createCommandSourceStack()
      .withPermission(4)
      .withSuppressedOutput();

    // Pre-flight modpack check (informational, non-blocking — never abort the install)
    try {
      StringBuilder preflight = new StringBuilder("[Install] Pre-flight modpack check:\n\n");
      appendVerifyReport(preflight, server, config);
      ctx.getSource().sendSuccess(
        () -> Component.literal(preflight.toString().trim()),
        false
      );
    } catch (Exception e) {
      LOGGER.warn("[Install] Pre-flight modpack check failed (continuing): {}", e.getMessage());
    }

    // Apply gamerules / difficulty
    for (var entry : config.gamerules.entrySet()) {
      String rule = entry.getKey();
      String value = entry.getValue();
      if (rule.equals("_difficulty")) {
        applyDifficulty(server, value, ctx);
      } else {
        server
          .getCommands()
          .performPrefixedCommand(silentOp, "gamerule " + rule + " " + value);
        ctx
          .getSource()
          .sendSuccess(
            () ->
              Component.literal("[Install] gamerule " + rule + " = " + value),
            true
          );
      }
    }

    // Force survival — this is a survival/hardcore experience. `defaultgamemode` also
    // persists to the world data, so the reloaded world starts new joins in survival.
    server.getCommands().performPrefixedCommand(silentOp, "defaultgamemode survival");
    server.getCommands().performPrefixedCommand(silentOp, "gamemode survival @a");
    ctx
      .getSource()
      .sendSuccess(() -> Component.literal("[Install] Game mode set to survival."), true);

    // Promote the world to hardcore (after gamerules, since hardcore locks difficulty to hard)
    boolean hardcoreFlipped = false;
    if (config.hardcore) {
      hardcoreFlipped = applyHardcore(server, ctx);
    }

    // Seed the CobbleDollars shop to its opening (0-badge) catalog. The per-badge unlock
    // progression GROWS from here as gym leaders / Acting CEO DJ fire their shop-tier rewards;
    // without this the world would start on the full catalog and the first badge would shrink it.
    if (ShopTierManager.applyTier(server, "badge_0")) {
      ctx
        .getSource()
        .sendSuccess(
          () ->
            Component.literal(
              "[Install] CobbleDollars shop seeded to opening catalog (badge_0)."
            ),
          true
        );
    }

    // Apply zones as safe zones + write Map Frontiers frontier file
    if (config.zones != null && !config.zones.isEmpty()) {
      NuzlockeConfig nzConfig = NuzlockeInit.getConfig();
      if (nzConfig == null) {
        ctx
          .getSource()
          .sendFailure(
            Component.literal(
              "[Install] Nuzlocke config not loaded — cannot apply zones."
            )
          );
      } else {
        Set<String> existing = nzConfig
          .getSafeZones()
          .stream()
          .map(z -> z.name.toLowerCase())
          .collect(Collectors.toSet());

        int added = 0;
        int skipped = 0;
        for (InstallZone iz : config.zones) {
          if (existing.contains(iz.name.toLowerCase())) {
            skipped++;
            continue;
          }
          NuzlockeConfig.SafeZone sz = new NuzlockeConfig.SafeZone(
            iz.name,
            iz.dimension,
            iz.derivedCenterX(),
            iz.centerY,
            iz.derivedCenterZ(),
            iz.derivedRadius(),
            iz.hostileOnly,
            iz.cylindrical
          );
          sz.announce = iz.announce;
          sz.subtitle = iz.subtitle != null ? iz.subtitle : "";
          sz.color = iz.color != null ? iz.color : "";
          nzConfig.getSafeZones().add(sz);
          added++;
        }
        nzConfig.save();

        final int a = added,
          s = skipped;
        ctx
          .getSource()
          .sendSuccess(
            () ->
              Component.literal(
                "[Install] Zones: " +
                  a +
                  " added, " +
                  s +
                  " skipped (name collision)."
              ),
            true
          );

        if (MapFrontiersBridge.isAvailable()) {
          // createNewGlobalFrontier needs a player as creator/owner; the command is
          // player-run in single-player, but fall back to any online player just in case.
          ServerPlayer owner = ctx.getSource().getPlayer();
          if (owner == null) {
            owner = server
              .getPlayerList()
              .getPlayers()
              .stream()
              .findFirst()
              .orElse(null);
          }
          int n = MapFrontiersBridge.createFrontiers(config.zones, owner);
          final int created = n;
          ctx
            .getSource()
            .sendSuccess(
              () ->
                Component.literal(
                  "[Install] Map Frontiers: " +
                    created +
                    " frontier(s) created."
                ),
              true
            );
        } else {
          ctx
            .getSource()
            .sendSuccess(
              () ->
                Component.literal(
                  "[Install] Map Frontiers not loaded — skipping frontier creation."
                ),
              false
            );
        }
      }
    }

    // Apply NPC presets via the existing npc-map storage (same as npc-map apply)
    NpcMapStorage storage = NpcMapInit.getStorage();
    if (storage == null || storage.size() == 0) {
      ctx
        .getSource()
        .sendSuccess(
          () ->
            Component.literal(
              "[Install] No NPC preset mappings registered yet. " +
                "Use '/cobblemon-initiative npc-map add' or run generate_npc_function " +
                "then '/function cobblemon_initiative:update_npc_presets'."
            ),
          false
        );
    } else {
      int applied = 0;
      for (NpcMapEntry e : storage.getAll()) {
        try {
          String cmd = "easy_npc preset import data " + e.uuid + " " + e.preset;
          server.getCommands().performPrefixedCommand(silentOp, cmd);
          applied++;
        } catch (Exception ex) {
          LOGGER.warn(
            "[Install] Failed to apply preset for {}: {}",
            e.uuid,
            ex.getMessage()
          );
        }
      }
      final int count = applied;
      ctx
        .getSource()
        .sendSuccess(
          () ->
            Component.literal(
              "[Install] Applied " +
                count +
                "/" +
                storage.size() +
                " NPC preset(s)."
            ),
          true
        );
    }

    // If hardcore was newly enabled, the client only learns it from a fresh login packet, so
    // disconnect every player — re-opening the world reloads the integrated server in hardcore
    // (permadeath) mode. Deferred to the next tick so this command finishes and its output is
    // flushed before the connection drops.
    if (hardcoreFlipped) {
      ctx
        .getSource()
        .sendSuccess(
          () ->
            Component.literal(
              "[Install] Hardcore newly enabled — disconnecting so the world reloads in hardcore mode. " +
                "Re-open the world to continue."
            ),
          true
        );
    }
    server.execute(() -> {
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
        p.connection.disconnect(
          Component.literal(
            "§6The Cobblemon Initiative\n\n§cmode enabled.\n§7Re-open the world to begin your adventure."
          )
        );
      }
    });

    return 1;
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static InstallConfig loadConfig(
    CommandContext<CommandSourceStack> ctx
  ) {
    try (
      InputStream stream = InstallCommand.class
        .getClassLoader()
        .getResourceAsStream(CONFIG_PATH)
    ) {
      if (stream == null) {
        ctx
          .getSource()
          .sendFailure(
            Component.literal(
              "[Install] install.json not found in mod resources."
            )
          );
        return null;
      }
      return GSON.fromJson(
        new InputStreamReader(stream, StandardCharsets.UTF_8),
        InstallConfig.class
      );
    } catch (Exception e) {
      ctx
        .getSource()
        .sendFailure(
          Component.literal(
            "[Install] Failed to load install.json: " + e.getMessage()
          )
        );
      LOGGER.error("Failed to load install.json", e);
      return null;
    }
  }

  /**
   * Promotes the running world to hardcore by flipping the {@code hardcore} flag on the live
   * {@link LevelSettings} (via the mixin accessors) and locking difficulty to hard. Hardcore is
   * normally fixed at world creation and the client only reads it from the login packet, so the
   * permadeath UI and spectator-on-death behaviour take full effect after the player rejoins.
   */
  private static boolean applyHardcore(
    MinecraftServer server,
    CommandContext<CommandSourceStack> ctx
  ) {
    WorldData worldData = server.getWorldData();
    if (!(worldData instanceof PrimaryLevelData)) {
      ctx
        .getSource()
        .sendFailure(
          Component.literal(
            "[Install] Could not enable hardcore — unexpected world data type " +
              worldData.getClass().getSimpleName() +
              "."
          )
        );
      return false;
    }

    LevelSettings settings = (
      (PrimaryLevelDataAccessor) (Object) worldData
    ).getSettings();
    boolean wasHardcore = settings.hardcore();
    if (!wasHardcore) {
      ((LevelSettingsAccessor) (Object) settings).setHardcore(true);
    }
    // Hardcore always runs on hard; lock it so it can't be lowered in-world.
    server.setDifficulty(Difficulty.HARD, true);
    server.setDifficultyLocked(true);

    ctx
      .getSource()
      .sendSuccess(
        () ->
          Component.literal(
            wasHardcore
              ? "[Install] World already hardcore — difficulty locked to HARD."
              : "[Install] Hardcore enabled — difficulty locked to HARD."
          ),
        true
      );
    // Only a fresh flip requires a relog; an already-hardcore world's client is already in sync.
    return !wasHardcore;
  }

  private static void applyDifficulty(
    MinecraftServer server,
    String name,
    CommandContext<CommandSourceStack> ctx
  ) {
    Difficulty diff = switch (name.toLowerCase()) {
      case "peaceful" -> Difficulty.PEACEFUL;
      case "easy" -> Difficulty.EASY;
      case "hard" -> Difficulty.HARD;
      default -> Difficulty.NORMAL;
    };
    server.setDifficulty(diff, true);
    ctx
      .getSource()
      .sendSuccess(
        () -> Component.literal("[Install] difficulty = " + name),
        true
      );
  }
}

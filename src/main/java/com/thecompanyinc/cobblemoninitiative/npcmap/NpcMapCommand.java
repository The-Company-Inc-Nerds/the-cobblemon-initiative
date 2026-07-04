package com.thecompanyinc.cobblemoninitiative.npcmap;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.thecompanyinc.cobblemoninitiative.util.EntityLookup;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;

public class NpcMapCommand {

  private static NpcMapStorage storage;

  // Shipped Easy NPC DATA presets: data/easy_npc/preset/** (Easy NPC 6.25's PresetSecurity
  // only accepts DATA imports from the easy_npc namespace under preset/).
  private static final String PRESET_PATH_PREFIX = "preset/";
  private static final String PRESET_SUFFIX       = ".npc.snbt";
  private static final String PRESET_NAMESPACE    = "easy_npc";

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher,
    CommandBuildContext buildContext,
    NpcMapStorage stor
  ) {
    storage = stor;

    dispatcher.register(
      Commands.literal("cobblemon-initiative")
        .then(
          Commands.literal("npc-map")
            .requires(src -> src.hasPermission(2))

            // /cobblemon-initiative npc-map add <uuid> <preset> [<label>]
            .then(
              Commands.literal("add")
                .then(
                  Commands.argument("uuid", StringArgumentType.word())
                    .suggests(NpcMapCommand::suggestLookedAt)
                    .then(
                      // string(): quotable, so "humanoid_slim/nalia_mom" works (word()
                      // rejects '/'); bare names imply humanoid/ via the resolver.
                      Commands.argument("preset", StringArgumentType.string())
                        .suggests(NpcMapCommand::suggestPresets)
                        .executes(ctx -> cmdAdd(ctx,
                          StringArgumentType.getString(ctx, "uuid"),
                          StringArgumentType.getString(ctx, "preset"),
                          ""))
                        .then(
                          Commands.argument("label", StringArgumentType.greedyString())
                            .executes(ctx -> cmdAdd(ctx,
                              StringArgumentType.getString(ctx, "uuid"),
                              StringArgumentType.getString(ctx, "preset"),
                              StringArgumentType.getString(ctx, "label")))
                        )
                    )
                )
            )

            // /cobblemon-initiative npc-map remove <uuid>
            .then(
              Commands.literal("remove")
                .then(
                  Commands.argument("uuid", StringArgumentType.word())
                    .suggests(NpcMapCommand::suggestStored)
                    .executes(ctx -> cmdRemove(ctx,
                      StringArgumentType.getString(ctx, "uuid")))
                )
            )

            // /cobblemon-initiative npc-map list
            .then(
              Commands.literal("list")
                .executes(NpcMapCommand::cmdList)
            )

            // /cobblemon-initiative npc-map apply
            .then(
              Commands.literal("apply")
                .executes(NpcMapCommand::cmdApply)
            )
        )
    );
  }

  // ---------------------------------------------------------------------------
  // Command handlers
  // ---------------------------------------------------------------------------

  private static int cmdAdd(
    CommandContext<CommandSourceStack> ctx,
    String uuidStr,
    String preset,
    String label
  ) {
    UUID uuid = parseUUID(ctx, uuidStr);
    if (uuid == null) return 0;

    if (storage.contains(uuid)) {
      ctx.getSource().sendFailure(
        Component.literal("[NPC Map] UUID already mapped: " + uuidStr
          + " → " + storage.get(uuid).preset)
      );
      return 0;
    }

    NpcMapEntry entry = new NpcMapEntry(uuid, preset, label);
    storage.put(entry);

    String display = label.isBlank() ? preset : preset + " (" + label + ")";
    ctx.getSource().sendSuccess(
      () -> Component.literal("[NPC Map] Added " + uuidStr + " → " + display),
      true
    );
    return 1;
  }

  private static int cmdRemove(CommandContext<CommandSourceStack> ctx, String uuidStr) {
    UUID uuid = parseUUID(ctx, uuidStr);
    if (uuid == null) return 0;

    if (storage.remove(uuid)) {
      ctx.getSource().sendSuccess(
        () -> Component.literal("[NPC Map] Removed " + uuidStr),
        true
      );
      return 1;
    }
    ctx.getSource().sendFailure(
      Component.literal("[NPC Map] UUID not found: " + uuidStr)
    );
    return 0;
  }

  private static int cmdList(CommandContext<CommandSourceStack> ctx) {
    if (storage.size() == 0) {
      ctx.getSource().sendSuccess(
        () -> Component.literal("[NPC Map] No mappings stored."), false
      );
      return 0;
    }

    StringBuilder sb = new StringBuilder("[NPC Map] Stored mappings (" + storage.size() + "):\n");
    for (NpcMapEntry e : storage.getAll()) {
      sb.append("  ").append(e.uuid)
        .append(" → ").append(e.preset);
      if (!e.label.isBlank()) sb.append(" (").append(e.label).append(")");
      sb.append("\n");
    }
    String msg = sb.toString().trim();
    ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
    return storage.size();
  }

  private static int cmdApply(CommandContext<CommandSourceStack> ctx) {
    MinecraftServer server = ctx.getSource().getServer();

    Collection<NpcMapEntry> entries = storage.getAll();
    if (entries.isEmpty()) {
      ctx.getSource().sendSuccess(
        () -> Component.literal("[NPC Map] No mappings stored. Use 'add' first."), false
      );
      return 0;
    }

    int count = 0;
    for (NpcMapEntry entry : entries) {
      try {
        // Only a LOADED NPC can be updated in place; the execute-as guard makes the
        // line a no-op for unloaded UUIDs (importing those would spawn a duplicate).
        String location = NpcPresetRefreshManager.resolvePresetLocation(entry.preset);
        String cmd = NpcPresetRefreshManager.importCommand(entry.uuid, location);
        CommandSourceStack src = server.createCommandSourceStack()
          .withPermission(4)
          .withSuppressedOutput();
        server.getCommands().performPrefixedCommand(src, cmd);
        count++;
      } catch (Exception e) {
        NpcMapInit.LOGGER.warn("[NPC Map] Failed to apply preset for {}: {}", entry.uuid, e.getMessage());
      }
    }

    final int applied = count;
    ctx.getSource().sendSuccess(
      () -> Component.literal("[NPC Map] Dispatched " + applied + "/" + entries.size()
        + " preset import(s) — loaded NPCs only."),
      true
    );
    return applied;
  }

  // ---------------------------------------------------------------------------
  // Suggestions
  // ---------------------------------------------------------------------------

  /**
   * Suggests the UUID of the entity the player is currently looking at
   * (within 10 blocks), with the entity's display name as tooltip.
   */
  private static CompletableFuture<Suggestions> suggestLookedAt(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    try {
      ServerPlayer player = ctx.getSource().getPlayerOrException();
      Entity target = EntityLookup.getEntityLookedAt(player, 10.0);
      if (target != null) {
        builder.suggest(
          target.getUUID().toString(),
          target.getDisplayName()
        );
      }
    } catch (Exception ignored) {}
    return builder.buildFuture();
  }

  /**
   * Suggests UUIDs already stored in the mapping.
   */
  private static CompletableFuture<Suggestions> suggestStored(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    for (NpcMapEntry entry : storage.getAll()) {
      String tooltip = entry.label.isBlank() ? entry.preset : entry.label;
      builder.suggest(entry.uuid.toString(), Component.literal(tooltip));
    }
    return builder.buildFuture();
  }

  /**
   * Suggests short preset names from the shipped Easy NPC DATA presets
   * (all *.npc.snbt files under data/easy_npc/preset/). Bare names imply humanoid/;
   * other entity-type paths are quoted so the string argument accepts the slash.
   */
  private static CompletableFuture<Suggestions> suggestPresets(
    CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder
  ) {
    try {
      MinecraftServer server = ctx.getSource().getServer();
      Map<ResourceLocation, Resource> resources = server.getResourceManager()
        .listResources(
          "preset",
          rl -> rl.getNamespace().equals(PRESET_NAMESPACE)
            && rl.getPath().endsWith(PRESET_SUFFIX)
        );
      for (ResourceLocation rl : resources.keySet()) {
        // rl.getPath() = "preset/humanoid/cyber_leader.npc.snbt"
        String path = rl.getPath();
        if (!path.startsWith(PRESET_PATH_PREFIX)) continue;
        String relative = path.substring(PRESET_PATH_PREFIX.length()); // "humanoid/cyber_leader.npc.snbt"
        String name = relative.substring(0, relative.length() - PRESET_SUFFIX.length()); // "humanoid/cyber_leader"
        if (name.startsWith("humanoid/")) {
          builder.suggest(name.substring("humanoid/".length()));
        } else {
          builder.suggest("\"" + name + "\"");
        }
      }
    } catch (Exception ignored) {}
    return builder.buildFuture();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static UUID parseUUID(CommandContext<CommandSourceStack> ctx, String str) {
    try {
      return UUID.fromString(str);
    } catch (IllegalArgumentException e) {
      ctx.getSource().sendFailure(
        Component.literal("[NPC Map] Invalid UUID: " + str)
      );
      return null;
    }
  }
}

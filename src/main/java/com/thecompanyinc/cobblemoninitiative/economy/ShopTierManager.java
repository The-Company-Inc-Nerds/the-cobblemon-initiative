package com.thecompanyinc.cobblemoninitiative.economy;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/**
 * Swaps the CobbleDollars default shop catalog to a pre-baked tier and hot-reloads it live.
 *
 * <p>Tier resources live at {@code /cobbledollars_tiers/<tier>.json} in the mod jar and are
 * regenerated from {@code scripts/shop_tiers/master_shop.json} by {@code scripts/generate_shop_tiers}.
 * Each is a complete CobbleDollars {@code default_shop} config: the items unlocked at that tier,
 * priced to the tier's {@code cd_instability}. {@link #applyTier} copies the chosen resource over
 * {@code <gameDir>/config/cobbledollars/default_shop.json} and runs {@code cobbledollars reload},
 * which CobbleDollars re-reads from disk and pushes to the live shop GUI (verified against
 * CobbleDollars 2.0.0+Beta-5.1: reload calls {@code ShopConfig.load()} then broadcasts the new shop).
 *
 * <p><b>Scope:</b> this only affects the DEFAULT shop. A {@code CobbleMerchant} carrying a custom
 * per-entity shop is served from its own NBT and is NOT touched by reload — in-world Pokémart
 * merchants must use the default shop for the swap to be visible.
 */
public final class ShopTierManager {

  private ShopTierManager() {}

  /** Tier ids in journey order. Mirrors the {@code tiers} list in {@code master_shop.json}. */
  public static final List<String> TIERS = List.of(
    "badge_0", "badge_1", "badge_2", "badge_3", "badge_4", "badge_5",
    "badge_6", "badge_7", "post_hq", "badge_8", "badge_9", "badge_10"
  );

  private static final String RESOURCE_PREFIX = "/cobbledollars_tiers/";

  /** Liberation relief: one relief level per this many liberated fields (mirrors the masters). */
  private static final int RELIEF_FIELDS_PER_LEVEL = 2;
  private static final int RELIEF_MAX_LEVEL = 2;

  /**
   * Resolve a base tier to its liberation-relief variant. Every {@code fieldsPerLevel}
   * liberated fields (the player-held {@code fields_liberated} score) upgrades the tier to
   * the pre-baked {@code <base>_relief<r>} catalog — cheaper CobbleDollar prices as the
   * player claws the currency back. Falls back level-by-level to the base tier when a
   * relief resource is absent (badge_0/1 have none). Explicit {@code _relief} requests
   * pass through untouched.
   */
  private static String resolveRelief(MinecraftServer server, String tier) {
    if (server == null || tier.contains("_relief")) return tier;
    int fields = 0;
    try {
      Scoreboard sb = server.getScoreboard();
      Objective obj = sb.getObjective("fields_liberated");
      if (obj == null) return tier;
      for (ServerPlayer p : server.getPlayerList().getPlayers()) {
        ReadOnlyScoreInfo info =
          sb.getPlayerScoreInfo(ScoreHolder.forNameOnly(p.getScoreboardName()), obj);
        if (info != null) fields = Math.max(fields, info.value());
      }
    } catch (Exception e) {
      return tier;
    }
    int level = Math.min(fields / RELIEF_FIELDS_PER_LEVEL, RELIEF_MAX_LEVEL);
    for (int r = level; r >= 1; r--) {
      String candidate = tier + "_relief" + r;
      try (InputStream probe =
          ShopTierManager.class.getResourceAsStream(RESOURCE_PREFIX + candidate + ".json")) {
        if (probe != null) return candidate;
      } catch (Exception ignored) {}
    }
    return tier;
  }

  /**
   * The base tier for a player's current progression state: {@code badge_<n>} through the
   * gym journey, {@code post_hq} once Acting CEO DJ is down (until badge 8 resumes the
   * ladder). Used by {@code /cobblemon-initiative shop refresh} to re-apply the active
   * catalog after world state (e.g. a field liberation) changes the relief level.
   */
  public static String currentBaseTier(ServerPlayer player) {
    int badges = 0;
    try {
      Scoreboard sb = player.getServer().getScoreboard();
      Objective obj = sb.getObjective("memory_fragment");
      if (obj != null) {
        ReadOnlyScoreInfo info =
          sb.getPlayerScoreInfo(ScoreHolder.forNameOnly(player.getScoreboardName()), obj);
        if (info != null) badges = Math.max(0, info.value());
      }
    } catch (Exception ignored) {}
    if (player.getTags().contains("defeated_villain_boss")) {
      return badges >= 8 ? "badge_" + Math.min(badges, 10) : "post_hq";
    }
    return "badge_" + Math.min(badges, 7);
  }

  /**
   * Copy the given tier over the CobbleDollars default shop config and reload it live.
   * The tier is first resolved to its liberation-relief variant (see {@link #resolveRelief}),
   * so callers keep passing base tiers ({@code badge_N} / {@code post_hq}) and the relief
   * follows the world state automatically.
   *
   * @return {@code true} if the tier resource was found and written; {@code false} on an unknown
   *     tier id or an IO failure (the shop is left unchanged).
   */
  public static boolean applyTier(MinecraftServer server, String tier) {
    tier = resolveRelief(server, tier);
    Path target = FabricLoader.getInstance()
      .getConfigDir()
      .resolve("cobbledollars")
      .resolve("default_shop.json");

    try (
      InputStream in =
        ShopTierManager.class.getResourceAsStream(RESOURCE_PREFIX + tier + ".json")
    ) {
      if (in == null) {
        InitiativeInit.LOGGER.warn("Shop tier resource not found: {}", tier);
        return false;
      }
      Files.createDirectories(target.getParent());
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to write shop tier {}", tier, e);
      return false;
    }

    if (server != null) {
      // CobbleDollars is a runtime modpack mod, not a compile dependency. If it is absent the
      // command simply fails to parse and is logged by the source — it does not throw here.
      server.getCommands().performPrefixedCommand(
        server.createCommandSourceStack().withPermission(2),
        "cobbledollars reload"
      );
      // Retier the Company Granary in lockstep: re-imports granary_keeper_<tier> onto every
      // recorded Granary NPC (functions generated by scripts/generate_granary_tiers; comment-only
      // no-ops until Granary UUIDs are recorded in npc_presets.json). Suppressed output — the
      // shop swap is the player-facing beat, the granary follows silently.
      server.getCommands().performPrefixedCommand(
        server.createCommandSourceStack().withPermission(2).withSuppressedOutput(),
        "function cobblemon_initiative:granary/apply_" + tier
      );
      InitiativeInit.LOGGER.info(
        "Applied shop tier {} (CobbleDollars reloaded, granary retiered).",
        tier
      );
    }
    return true;
  }
}

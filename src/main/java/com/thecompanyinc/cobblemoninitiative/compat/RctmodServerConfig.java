package com.thecompanyinc.cobblemoninitiative.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guarantees the rctmod level-cap / series settings this mod depends on, STANDALONE —
 * without the mrpack's baked world config. All via reflection: rctmod is a runtime-only
 * mod (no compile dependency).
 *
 * <p><b>Why:</b> we enforce our own badge level-cap ladder (a Cobblemon
 * {@code EXPERIENCE_GAINED_EVENT_PRE} clamp; see {@code InitiativeInit}). rctmod ALSO
 * clamps XP unless {@code allowOverLeveling=true}, and with both active the LOWER cap
 * wins — reintroducing rctmod's default frozen cap (derived from {@code initialLevelCap}
 * 15 under {@code initialSeries="empty"}). rctmod's config is a PER-WORLD serverconfig,
 * so only the bundled UPM 2 world has it baked; every other world (fresh world, dev
 * client, bare-mod install) gets rctmod defaults and breaks. This heals it at runtime.
 *
 * <p><b>Bytecode-verified (rctmod 0.18.1, ForgeConfigAPIPort 21.1.6):</b>
 * {@code ServerConfig} getters read CACHED primitives ({@code *Cached} fields), not the
 * live {@code ConfigValue}; the cache is baked from the toml during SERVER_STARTING and
 * only re-baked by {@code reload()}. So a {@code ConfigValue.set()} is a no-op for the
 * getters — we write the {@code *Cached} fields directly. By SERVER_STARTED the config is
 * loaded and cache-populated, no player has joined, and no XP event has fired: the correct
 * hook. Setting {@code allowOverLevelingCached=true} makes rctmod's clamp return early,
 * leaving only our ladder; {@code initialSeriesCached} then auto-places new players into
 * our series (needed for the {@code rctmod player add progress} dispatch to register wins).
 */
public final class RctmodServerConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final String SERIES = "cobblemon-initiative";

  private RctmodServerConfig() {}

  public static boolean present() {
    return FabricLoader.getInstance().isModLoaded("rctmod");
  }

  /** SERVER_STARTED: force allowOverLeveling + our series into rctmod's live cache. */
  public static void healServerConfig(MinecraftServer server) {
    if (!present()) return;
    try {
      Object config = serverConfig();
      Class<?> k = config.getClass();
      setBool(k, config, "allowOverLevelingCached", true);
      setStr(k, config, "initialSeriesCached", SERIES);
      // Neutralize the 15 fallback too, so even if rctmod's clamp is ever reached its
      // cap can never sit below ours (our ladder tops out at 100).
      setIntIfPresent(k, config, "initialLevelCapCached", 100);
      // Disable rctmod's own trainer SPAWNING on any world too — our shipped trainer
      // JSONs are already spawnWeightFactor:0, but zeroing the global chance means the
      // per-world serverconfig/rctmod-server.toml is now FULLY irrelevant (a map swap
      // that resets it to rctmod defaults changes nothing).
      setDoubleIfPresent(k, config, "globalSpawnChanceCached", 0.0);
      setDoubleIfPresent(k, config, "globalSpawnChanceMinimumCached", 0.0);
      LOGGER.info("[rctmod compat] Healed server config: allowOverLeveling=true, "
        + "initialSeries={}, spawning off (badge level-cap ladder enforced by this mod).", SERIES);
    } catch (Throwable t) {
      LOGGER.warn("[rctmod compat] Could not heal rctmod server config — if the world's "
        + "serverconfig/rctmod-server.toml lacks allowOverLeveling=true, the level cap may "
        + "be wrong. This is non-fatal.", t);
    }
  }

  /** On join: migrate players already saved under the wrong/empty series. New players are
   *  placed by initialSeriesCached (healed above); this only fixes pre-existing stat.dat. */
  public static void ensurePlayerSeries(ServerPlayer player) {
    if (!present()) return;
    try {
      Object data = trainerData(player);
      if (data == null) return;
      Object cur = data.getClass().getMethod("getCurrentSeries").invoke(data);
      if (!SERIES.equals(cur)) {
        data.getClass().getMethod("setCurrentSeries", String.class).invoke(data, SERIES);
        LOGGER.info("[rctmod compat] Placed {} into series {} (was {}).",
          player.getGameProfile().getName(), SERIES, cur);
      }
    } catch (Throwable t) {
      // getData isn't ready at connection time on some paths — harmless: new players are
      // already placed by the healed initialSeries; this only migrates old wrong-series
      // saves. Quiet by design (no stack spam), enforcement is unaffected.
      LOGGER.debug("[rctmod compat] deferred series check for {}: {}",
        player.getGameProfile().getName(), t.toString());
    }
  }

  // ─── reflection helpers ────────────────────────────────────────────────────

  private static Object serverConfig() throws Exception {
    Class<?> rct = Class.forName("com.gitlab.srcmc.rctmod.api.RCTMod");
    Object inst = rct.getMethod("getInstance").invoke(null);
    return rct.getMethod("getServerConfig").invoke(inst);
  }

  private static Object trainerData(ServerPlayer player) throws Exception {
    Class<?> rct = Class.forName("com.gitlab.srcmc.rctmod.api.RCTMod");
    Object inst = rct.getMethod("getInstance").invoke(null);
    Object tm = rct.getMethod("getTrainerManager").invoke(inst);
    for (Method m : tm.getClass().getMethods()) {
      if (m.getName().equals("getData") && m.getParameterCount() == 1
          && m.getParameterTypes()[0].isAssignableFrom(player.getClass())) {
        return m.invoke(tm, player);
      }
    }
    return null;
  }

  private static void setBool(Class<?> k, Object o, String field, boolean v) throws Exception {
    Field f = k.getDeclaredField(field);
    f.setAccessible(true);
    f.setBoolean(o, v);
  }

  private static void setStr(Class<?> k, Object o, String field, String v) throws Exception {
    Field f = k.getDeclaredField(field);
    f.setAccessible(true);
    f.set(o, v);
  }

  private static void setIntIfPresent(Class<?> k, Object o, String field, int v) {
    try {
      Field f = k.getDeclaredField(field);
      f.setAccessible(true);
      f.setInt(o, v);
    } catch (NoSuchFieldException ignored) {
      // field name drift across rctmod builds — non-fatal, allowOverLeveling already covers it
    } catch (Exception e) {
      LOGGER.debug("[rctmod compat] {} not set: {}", field, e.toString());
    }
  }

  private static void setDoubleIfPresent(Class<?> k, Object o, String field, double v) {
    try {
      Field f = k.getDeclaredField(field);
      f.setAccessible(true);
      f.setDouble(o, v);
    } catch (NoSuchFieldException ignored) {
      // field name drift — non-fatal, spawnWeightFactor:0 on our trainer JSONs already covers it
    } catch (Exception e) {
      LOGGER.debug("[rctmod compat] {} not set: {}", field, e.toString());
    }
  }
}

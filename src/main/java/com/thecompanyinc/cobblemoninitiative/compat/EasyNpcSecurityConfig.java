package com.thecompanyinc.cobblemoninitiative.compat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guarantees Easy NPC's ExecAsUser command allowlist STANDALONE — without the mrpack's
 * config override. Easy NPC 6.25 blocks EVERY dialog-button command whose root is not in
 * {@code executeAsUserCommandAllowList.*} (empty list = block all), which would kill all
 * quest buttons, payouts, and battles on a bare-mod install.
 *
 * <p>Timing: Easy NPC reads security.cfg LAZILY (SecurityConfig static init fires on the
 * first ExecAsUser dispatch, i.e. the first dialog button press), so patching during our
 * {@code onInitialize()} reliably precedes the first read — no restart required.
 *
 * <p>Merge, never clobber: existing entries and every other property (feature roles,
 * command levels, unsafe-command settings) are preserved; only missing roots are added.
 */
public final class EasyNpcSecurityConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");

  private static final String[] ALLOWLIST_KEYS = {
    "executeAsUserCommandAllowList.ALL",
    "executeAsUserCommandAllowList.MODERATORS",
    "executeAsUserCommandAllowList.GAMEMASTERS",
    "executeAsUserCommandAllowList.ADMINS",
    "executeAsUserCommandAllowList.OWNERS",
  };

  /** Every command root a shipped preset dialog button dispatches via ExecAsUser. */
  private static final Set<String> REQUIRED_ROOTS = Set.of(
    "advancement", "clear", "cobbledollars", "cobblemon-initiative", "easy_npc",
    "execute", "function", "give", "givepokemonother", "healpokemon", "loot",
    "openstarterscreen", "particle", "playsound", "scoreboard", "tag", "tbcs",
    "tellraw", "title"
  );

  private EasyNpcSecurityConfig() {}

  /** No-op when Easy NPC is absent. Safe to call multiple times. */
  public static void ensureAllowlist() {
    if (!FabricLoader.getInstance().isModLoaded("easy_npc")
        && !FabricLoader.getInstance().isModLoaded("easynpc")) {
      return;
    }

    Path file = FabricLoader.getInstance().getConfigDir().resolve("easy_npc").resolve("security.cfg");
    try {
      Properties props = new Properties();
      if (Files.exists(file)) {
        try (InputStream in = Files.newInputStream(file)) {
          props.load(in);
        }
      }

      boolean changed = false;
      for (String key : ALLOWLIST_KEYS) {
        Set<String> roots = new TreeSet<>();
        String existing = props.getProperty(key, "");
        for (String root : existing.split(",")) {
          String trimmed = root.trim().toLowerCase();
          if (!trimmed.isEmpty()) roots.add(trimmed);
        }
        if (roots.addAll(REQUIRED_ROOTS)) {
          props.setProperty(key, String.join(",", roots));
          changed = true;
        }
      }

      if (changed) {
        Files.createDirectories(file.getParent());
        try (OutputStream out = Files.newOutputStream(file)) {
          props.store(out,
            "Easy NPC Security Configuration\n"
            + "executeAsUserCommandAllowList.* entries below are REQUIRED by Cobblemon Initiative\n"
            + "(Easy NPC blocks every dialog-button command whose root is not allowlisted).\n"
            + "The Initiative re-adds them on launch if removed; other settings are yours.");
        }
        LOGGER.info(
          "[Easy NPC compat] Patched {} — ensured {} ExecAsUser command root(s) on {} allowlist key(s).",
          file, REQUIRED_ROOTS.size(), ALLOWLIST_KEYS.length
        );
      }
    } catch (Exception e) {
      LOGGER.error(
        "[Easy NPC compat] Could not ensure the ExecAsUser allowlist in {} — NPC dialog "
        + "buttons may be silently blocked. Fix the file manually or delete it and relaunch.",
        file, e
      );
    }
  }

  /** For `install check`: true when every allowlist key already carries the required roots. */
  public static boolean isAllowlistComplete() {
    Path file = FabricLoader.getInstance().getConfigDir().resolve("easy_npc").resolve("security.cfg");
    if (!Files.exists(file)) return false;
    try {
      Properties props = new Properties();
      try (InputStream in = Files.newInputStream(file)) {
        props.load(in);
      }
      for (String key : ALLOWLIST_KEYS) {
        Set<String> roots = new TreeSet<>();
        for (String root : props.getProperty(key, "").split(",")) {
          String trimmed = root.trim().toLowerCase();
          if (!trimmed.isEmpty()) roots.add(trimmed);
        }
        if (!roots.containsAll(REQUIRED_ROOTS)) return false;
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}

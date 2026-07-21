package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream-sync knobs — the OBS overlay push subsystem ({@code streamsync/}).
 *
 * Default-DISABLED: a bare mod install never starts the pusher thread, never
 * creates a network object, and every hook no-ops on a null check. The UPM 2
 * mrpack ships an override of this file (mrpack/overrides/config/) with
 * enabled=true + the broadcast-host endpoint, so only the pack instance comes
 * up streaming. Cached singleton read pattern (per {@link ProgressionConfig});
 * refreshed by {@link #reload()} from the ModMenu save runnable and
 * {@code /streamsync reload}.
 */
public class StreamSyncConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon-initiative");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final File CONFIG_FILE = new File(
    "config/cobblemon-initiative-streamsync.json"
  );

  private static StreamSyncConfig instance;

  /** Master switch. False = the subsystem is inert (no thread, no sockets, no allocation). */
  private boolean enabled = false;

  /** The overlay service's ingest endpoint (POST). Push-only — the game box needs no open port. */
  private String endpointUrl = "http://10.1.10.15:8082/ingest";

  /** Optional bearer token sent with every push. Blank = none (the firewall is the gate). */
  private String authToken = "";

  /** How often the snapshot builder checks game state for changes. 40 = every 2 seconds. */
  private int snapshotIntervalTicks = 40;

  /** Unchanged state is still re-pushed this often so the overlay's staleness watchdog has a pulse. */
  private int heartbeatSeconds = 15;

  /** Per-request connect + response ceiling, so a dead endpoint can never stall the pusher long. */
  private int requestTimeoutMs = 3000;

  /** Event-queue cap while the endpoint is down (oldest dropped; snapshots never stack — latest wins). */
  private int maxQueuedMessages = 256;

  /** Include each party member's held item id in snapshots. */
  private boolean includeHeldItems = true;

  /** Include the announced zone name (Nuzlocke install.json zones) in snapshots. */
  private boolean includeLocation = true;

  // ── Untracked .env overrides (transient — never serialized back) ──────────────
  /**
   * The endpoint / token / enable flag are secrets the pack must NOT commit to the tracked
   * config JSON. They can instead be supplied by an untracked env file dropped in the instance
   * ("mrpack") folder — {@code .env} at the game root or {@code config/streamsync.env} — or by a
   * real process env var. These are applied on top of the JSON on {@link #load()} and held in
   * {@code transient} fields so {@link #save()} (Gson skips transient) never writes them back.
   * Precedence: JSON default &lt; config/streamsync.env &lt; .env &lt; process env.
   */
  private transient String envEndpointUrl;
  private transient String envAuthToken;
  private transient Boolean envEnabled;

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  public static StreamSyncConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static StreamSyncConfig load() {
    StreamSyncConfig cfg = null;
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          cfg = GSON.fromJson(reader, StreamSyncConfig.class);
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[StreamSync] Error loading config, using defaults: {}", e.getMessage());
    }
    if (cfg == null) {
      cfg = new StreamSyncConfig();
      cfg.save(); // writes DEFAULTS only — env overrides are applied AFTER, into transient fields
    }
    cfg.applyEnvOverrides();
    return cfg;
  }

  // ── .env override layering ────────────────────────────────────────────────────

  /** Layer untracked env sources over the JSON (lowest→highest precedence), into transient fields. */
  private void applyEnvOverrides() {
    applyEnvFile(new File("config/streamsync.env"));
    applyEnvFile(new File(".env")); // the instance ("mrpack") root drop the broadcaster asked for
    applyKV("STREAMSYNC_ENDPOINT_URL", System.getenv("STREAMSYNC_ENDPOINT_URL"));
    applyKV("STREAMSYNC_AUTH_TOKEN", System.getenv("STREAMSYNC_AUTH_TOKEN"));
    applyKV("STREAMSYNC_ENABLED", System.getenv("STREAMSYNC_ENABLED"));
    if (envEndpointUrl != null || envAuthToken != null || envEnabled != null) {
      LOGGER.info("[StreamSync] Applied .env override(s): endpoint={} token={} enabled={}",
        envEndpointUrl != null, envAuthToken != null, envEnabled);
    }
  }

  private void applyEnvFile(File f) {
    if (f == null || !f.isFile()) return;
    try {
      for (String raw : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
        String line = raw.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        int eq = line.indexOf('=');
        if (eq <= 0) continue;
        applyKV(line.substring(0, eq).trim(), stripQuotes(line.substring(eq + 1).trim()));
      }
    } catch (IOException e) {
      LOGGER.warn("[StreamSync] Could not read env override {}: {}", f, e.getMessage());
    }
  }

  private void applyKV(String key, String val) {
    if (key == null || val == null) return;
    val = val.trim();
    if (val.isEmpty()) return;
    switch (key) {
      case "STREAMSYNC_ENDPOINT_URL" -> this.envEndpointUrl = val;
      case "STREAMSYNC_AUTH_TOKEN" -> this.envAuthToken = val;
      case "STREAMSYNC_ENABLED" -> this.envEnabled = Boolean.parseBoolean(val);
      default -> { /* not an override key */ }
    }
  }

  private static String stripQuotes(String s) {
    if (s.length() >= 2
        && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }

  public void save() {
    try {
      CONFIG_FILE.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
      }
    } catch (IOException e) {
      LOGGER.error("[StreamSync] Error saving config: {}", e.getMessage());
    }
  }

  // ── Getters / setters ───────────────────────────────────────────────────────

  // Env overrides win at read time (StreamSyncPusher reads these at construction; /streamsync
  // reload re-runs load() so a changed .env is re-layered).
  public boolean isEnabled() { return envEnabled != null ? envEnabled : enabled; }
  public String getEndpointUrl() { return envEndpointUrl != null ? envEndpointUrl : endpointUrl; }
  public String getAuthToken() { return envAuthToken != null ? envAuthToken : authToken; }
  public int getSnapshotIntervalTicks() { return snapshotIntervalTicks; }
  public int getHeartbeatSeconds() { return heartbeatSeconds; }
  public int getRequestTimeoutMs() { return requestTimeoutMs; }
  public int getMaxQueuedMessages() { return maxQueuedMessages; }
  public boolean isIncludeHeldItems() { return includeHeldItems; }
  public boolean isIncludeLocation() { return includeLocation; }

  public void setEnabled(boolean v) { this.enabled = v; }
  public void setEndpointUrl(String v) { this.endpointUrl = v; }
  public void setAuthToken(String v) { this.authToken = v; }
  public void setSnapshotIntervalTicks(int v) { this.snapshotIntervalTicks = v; }
  public void setHeartbeatSeconds(int v) { this.heartbeatSeconds = v; }
  public void setRequestTimeoutMs(int v) { this.requestTimeoutMs = v; }
  public void setMaxQueuedMessages(int v) { this.maxQueuedMessages = v; }
  public void setIncludeHeldItems(boolean v) { this.includeHeldItems = v; }
  public void setIncludeLocation(boolean v) { this.includeLocation = v; }
}

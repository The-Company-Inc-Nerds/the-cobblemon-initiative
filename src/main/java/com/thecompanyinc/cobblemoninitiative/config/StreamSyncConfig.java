package com.thecompanyinc.cobblemoninitiative.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

  // ── Singleton / lifecycle ─────────────────────────────────────────────────────

  public static StreamSyncConfig get() {
    if (instance == null) instance = load();
    return instance;
  }

  public static void reload() {
    instance = load();
  }

  public static StreamSyncConfig load() {
    try {
      if (CONFIG_FILE.exists()) {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
          StreamSyncConfig cfg = GSON.fromJson(reader, StreamSyncConfig.class);
          if (cfg != null) return cfg;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("[StreamSync] Error loading config, using defaults: {}", e.getMessage());
    }
    StreamSyncConfig cfg = new StreamSyncConfig();
    cfg.save();
    return cfg;
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

  public boolean isEnabled() { return enabled; }
  public String getEndpointUrl() { return endpointUrl; }
  public String getAuthToken() { return authToken; }
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

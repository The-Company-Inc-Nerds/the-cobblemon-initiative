package com.thecompanyinc.cobblemoninitiative.streamsync;

import com.google.gson.JsonObject;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import com.thecompanyinc.cobblemoninitiative.config.StreamSyncConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The wire — one daemon thread draining a bounded queue of pre-built
 * JsonObjects into synchronous HTTP POSTs against the overlay's /ingest.
 *
 * <p>Contract:
 * <ul>
 * <li>The pusher NEVER touches game objects. Callers build the complete
 *     JsonObject on their own thread; this class only stamps the envelope
 *     (session/seq/t — at ENQUEUE time, on the calling thread, so wire order is
 *     fixed the moment the caller posts), serializes, and ships.</li>
 * <li>Queue discipline: at most ONE queued snapshot (a newer one REPLACES an
 *     unsent older one — only the latest state matters), events FIFO capped at
 *     maxQueuedMessages with the oldest dropped.</li>
 * <li>Failure discipline: exponential backoff 1s → 2s → 5s → 15s, and exactly
 *     one WARN per link transition (down, then recovered) — a 4-hour stream
 *     with the overlay off must not fill the log.</li>
 * <li>{@link #shutdown} is a HARD ceiling — world save is never held hostage
 *     by a flush.</li>
 * </ul>
 */
public class StreamSyncPusher {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitiativeInit.MOD_ID);
  private static final long[] BACKOFF_MILLIS = {1000, 2000, 5000, 15000};

  private final String sessionId;
  private final URI endpoint;
  private final String authToken;
  private final int requestTimeoutMs;
  private final int maxQueuedMessages;
  private final HttpClient http;
  private final Thread thread;
  private final AtomicLong seq = new AtomicLong();

  /** Guards {@link #events}, {@link #pendingSnapshot}, {@link #stopping}; doubles as the wait/notify hub. */
  private final Object lock = new Object();
  private final ArrayDeque<JsonObject> events = new ArrayDeque<>();
  private JsonObject pendingSnapshot;
  private boolean stopping = false;

  /** Starts optimistic so the very first failure logs the down transition. Pusher thread writes, status reads. */
  private volatile boolean linkUp = true;
  private int failStreak = 0; // pusher thread only

  public StreamSyncPusher(String sessionId, StreamSyncConfig cfg) {
    this.sessionId = sessionId;
    this.endpoint = URI.create(cfg.getEndpointUrl());
    this.authToken = cfg.getAuthToken() == null ? "" : cfg.getAuthToken();
    this.requestTimeoutMs = Math.max(250, cfg.getRequestTimeoutMs());
    this.maxQueuedMessages = Math.max(1, cfg.getMaxQueuedMessages());
    this.http = HttpClient.newBuilder()
      .connectTimeout(Duration.ofMillis(this.requestTimeoutMs))
      .build();
    this.thread = new Thread(this::run, "cobblemon-initiative-streamsync");
    this.thread.setDaemon(true);
    this.thread.start();
  }

  // ── Enqueue (any thread) ─────────────────────────────────────────────────────

  public void enqueueEvent(JsonObject json) {
    synchronized (lock) {
      if (stopping) return;
      stamp(json); // inside the lock: seq assignment is atomic with insertion, so FIFO order == seq order
      while (events.size() >= maxQueuedMessages) events.pollFirst(); // oldest dropped
      events.addLast(json);
      lock.notifyAll();
    }
  }

  public void enqueueSnapshot(JsonObject json) {
    synchronized (lock) {
      if (stopping) return;
      stamp(json);
      pendingSnapshot = json; // replace, never stack — only the latest state matters
      lock.notifyAll();
    }
  }

  /** Envelope stamp — session identity + monotonic ordering, fixed at enqueue time. Caller holds {@link #lock}. */
  private void stamp(JsonObject json) {
    json.addProperty("session", sessionId);
    json.addProperty("seq", seq.incrementAndGet());
    json.addProperty("t", System.currentTimeMillis());
  }

  // ── Status (server thread — /streamsync status) ──────────────────────────────

  public String getSessionId() {
    return sessionId;
  }

  public long lastSeq() {
    return seq.get();
  }

  public boolean isLinkUp() {
    return linkUp;
  }

  public int queuedCount() {
    synchronized (lock) {
      return events.size() + (pendingSnapshot != null ? 1 : 0);
    }
  }

  // ── Shutdown (server thread) ─────────────────────────────────────────────────

  /**
   * Signals stop, lets the thread flush what it can, and returns within
   * {@code maxWaitMillis} no matter what. The interrupt near the deadline
   * breaks any in-flight send (java.net.http honours interrupts) so a dead
   * endpoint can never push past the ceiling.
   */
  public void shutdown(long maxWaitMillis) {
    synchronized (lock) {
      stopping = true;
      lock.notifyAll();
    }
    long interruptAt = Math.max(0, maxWaitMillis - 500);
    try {
      thread.join(interruptAt);
      if (thread.isAlive()) {
        thread.interrupt();
        thread.join(Math.max(1, maxWaitMillis - interruptAt));
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  // ── Pusher thread ────────────────────────────────────────────────────────────

  private void run() {
    while (true) {
      JsonObject msg;
      synchronized (lock) {
        while (!stopping && events.isEmpty() && pendingSnapshot == null) {
          try {
            lock.wait();
          } catch (InterruptedException e) {
            return;
          }
        }
        if (events.isEmpty() && pendingSnapshot == null) return; // stopping + fully drained
        msg = takeLowestSeq();
      }
      if (!post(msg)) {
        synchronized (lock) {
          if (stopping) return; // shutdown flush is best-effort — drop, never delay world save
          requeue(msg);
        }
        sleepBackoff();
        synchronized (lock) {
          if (stopping) return;
        }
      }
    }
  }

  /**
   * Emission stays seq-ordered even with the snapshot slot beside the event
   * queue — the overlay dedups on a per-session lastSeq, so a late lower-seq
   * message would look like a replay and be dropped. Caller holds {@link #lock}.
   */
  private JsonObject takeLowestSeq() {
    if (pendingSnapshot == null) return events.pollFirst();
    if (events.isEmpty()) {
      JsonObject snap = pendingSnapshot;
      pendingSnapshot = null;
      return snap;
    }
    if (events.peekFirst().get("seq").getAsLong() < pendingSnapshot.get("seq").getAsLong()) {
      return events.pollFirst();
    }
    JsonObject snap = pendingSnapshot;
    pendingSnapshot = null;
    return snap;
  }

  /** Puts a failed message back for retry. Caller holds {@link #lock}. */
  private void requeue(JsonObject msg) {
    if ("snapshot".equals(msg.get("type").getAsString())) {
      if (pendingSnapshot == null) pendingSnapshot = msg; // a newer snapshot already won otherwise
    } else {
      events.addFirst(msg); // back to the head — FIFO order holds; enqueue enforces the cap
    }
  }

  private boolean post(JsonObject msg) {
    try {
      HttpRequest.Builder request = HttpRequest.newBuilder(endpoint)
        .timeout(Duration.ofMillis(requestTimeoutMs))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(msg.toString(), StandardCharsets.UTF_8));
      if (!authToken.isEmpty()) request.header("Authorization", "Bearer " + authToken);
      HttpResponse<Void> response = http.send(request.build(), HttpResponse.BodyHandlers.discarding());
      boolean ok = response.statusCode() / 100 == 2;
      if (ok) {
        onSuccess();
      } else {
        onFailure("HTTP " + response.statusCode());
      }
      return ok;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // shutdown deadline hit mid-send
      return false;
    } catch (Exception e) {
      onFailure(e.getClass().getSimpleName() + ": " + e.getMessage());
      return false;
    }
  }

  private void onSuccess() {
    failStreak = 0;
    if (!linkUp) {
      linkUp = true;
      LOGGER.warn("[StreamSync] Overlay endpoint recovered — resuming pushes.");
    }
  }

  private void onFailure(String reason) {
    failStreak++;
    if (linkUp) {
      linkUp = false;
      LOGGER.warn(
        "[StreamSync] Overlay endpoint unreachable ({}) — backing off 1s→15s, retrying quietly.",
        reason
      );
    }
  }

  /**
   * Timed wait against a fixed deadline (enqueue notifies must NOT shortcut the
   * backoff — a steady snapshot cadence would otherwise hammer a dead endpoint).
   * A stop signal does break out early.
   */
  private void sleepBackoff() {
    long deadline = System.currentTimeMillis()
      + BACKOFF_MILLIS[Math.min(Math.max(failStreak, 1) - 1, BACKOFF_MILLIS.length - 1)];
    synchronized (lock) {
      long remaining;
      while (!stopping && (remaining = deadline - System.currentTimeMillis()) > 0) {
        try {
          lock.wait(remaining);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }
}

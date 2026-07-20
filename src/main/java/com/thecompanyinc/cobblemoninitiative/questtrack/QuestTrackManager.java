package com.thecompanyinc.cobblemoninitiative.questtrack;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/**
 * Quest tracking: the player cycles through the active sidebar quests (] / [ keybinds →
 * the /cobblemon-initiative track commands); the tracked line gets an aqua "▶ "
 * highlight, and the tracked quest's current objective is published as a waypoint —
 * picked up by the client-thread poller (QuestTrackClient) for JourneyMap, with an
 * END_ROD beam fallback here when JourneyMap is absent.
 *
 * <p>The ACTIVE quest list is never re-derived from quest conditions: a quest is active
 * iff its holder currently has a score on the `ci_quest` objective (render.mcfunction
 * owns those scores), ordered by score DESC — the sidebar order (q.main=100 first).
 */
public class QuestTrackManager {

  private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()
    .create();
  // quest_waypoints.json uses snake_case keys (if_tags / not_tags).
  private static final Gson REGISTRY_GSON = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .create();
  private static final String TRACKING_FILE_NAME =
    "cobblemon_initiative_quest_tracking.json";
  private static final String QUEST_OBJECTIVE = "ci_quest";
  private static final String HIGHLIGHT_PREFIX = "▶ ";
  private static final int BEAM_HEIGHT = 14;

  /** Latest resolved waypoint per tracking player. Lives in a ConcurrentHashMap because
   *  the client-thread poller (QuestTrackClient) reads it cross-thread via current(). */
  private static final Map<UUID, TrackedWaypoint> published =
    new ConcurrentHashMap<>();

  private final List<QuestDef> quests = new ArrayList<>();
  private final Map<UUID, String> trackedHolders = new HashMap<>();
  /** Quest holders that had a ci_quest score on the previous refresh — a durable holder that
   *  drops out of this set has been completed (the score's removal is the completion signal).
   *  Seeds itself on the first pass so pre-loaded active quests are never miscounted. */
  private Set<String> lastActiveHolders = new HashSet<>();
  private boolean questLedgerSeeded = false;
  /** Holders whose ci_quest score can vanish WITHOUT the quest being completed, so a
   *  disappearance here is NOT a completion: the free-retry timed mini-games (their run tag is
   *  cleared on fail/expire exactly as on a win — cascade/sprint/derby) and the display-only
   *  Verified-Growth line (rides a fluctuating instability threshold, no completion latch). The
   *  count means "durable quests resolved", so these are excluded rather than over-counted. */
  private static final Set<String> VOLATILE_QUEST_HOLDERS = Set.of(
    "q.side_ascent", "q.side_sprint", "q.side_classic", "q.side_verified"
  );
  /** Sidebar lines currently carrying the ▶ highlight: holder → pre/post components. */
  private final Map<String, HighlightState> highlights = new HashMap<>();
  /** The beam fallback only runs when JourneyMap is absent (waypoints cover it else). */
  private final boolean particleFallback =
    !FabricLoader.getInstance().isModLoaded("journeymap");
  private Path savePath;
  private int tickCounter = 0;

  /** The tracked quest's resolved objective. hasPos=false = highlight-only stage. */
  public record TrackedWaypoint(
    String holder,
    String questName,
    String label,
    boolean hasPos,
    double x,
    double y,
    double z
  ) {}

  /** Cross-thread read for the client poller; null when the player tracks nothing. */
  public static TrackedWaypoint current(ServerPlayer player) {
    return published.get(player.getUUID());
  }

  // ---------------------------------------------------------------------------
  // Registry (bundled quest_waypoints.json — read like ConfigLoader reads its JSONs)
  // ---------------------------------------------------------------------------

  public void loadQuests() {
    quests.clear();
    try {
      InputStream stream = getClass()
        .getClassLoader()
        .getResourceAsStream(
          "data/cobblemon_initiative/quest_waypoints.json"
        );
      if (stream != null) {
        InputStreamReader reader = new InputStreamReader(
          stream,
          StandardCharsets.UTF_8
        );
        QuestRegistry registry = REGISTRY_GSON.fromJson(
          reader,
          QuestRegistry.class
        );
        if (registry != null && registry.quests != null) {
          for (QuestDef quest : registry.quests) {
            if (quest != null && quest.holder != null && quest.name != null) {
              quests.add(quest);
            }
          }
        }
        reader.close();
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to load quest waypoints config", e);
    }

    int stages = quests.stream().mapToInt(q -> q.stages.size()).sum();
    InitiativeInit.LOGGER.info(
      "Loaded {} trackable quests ({} stages)",
      quests.size(),
      stages
    );
  }

  // ---------------------------------------------------------------------------
  // Lifecycle (load-on-start / save-on-stop, world-dir Gson persistence)
  // ---------------------------------------------------------------------------

  public void load(MinecraftServer server) {
    savePath = server.getWorldPath(LevelResource.ROOT).resolve(
      TRACKING_FILE_NAME
    );
    trackedHolders.clear();
    highlights.clear();
    published.clear();
    tickCounter = 0;
    // This manager is a process-lifetime singleton; load() runs on every SERVER_STARTED,
    // including a single-player quit-to-title into a DIFFERENT save. Reset the completion
    // ledger's session state too, or world B's first pass would diff world A's stale holder
    // set (and, because ci_quest scores populate lazily, mass-credit phantom completions).
    lastActiveHolders = new HashSet<>();
    questLedgerSeeded = false;

    if (Files.exists(savePath)) {
      try (
        Reader reader = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)
      ) {
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> data = GSON.fromJson(reader, type);
        if (data != null) {
          for (Map.Entry<String, String> entry : data.entrySet()) {
            trackedHolders.put(UUID.fromString(entry.getKey()), entry.getValue());
          }
        }
      } catch (Exception e) {
        InitiativeInit.LOGGER.error("Failed to load quest tracking data", e);
      }
    }
  }

  public void save(MinecraftServer server) {
    // The ▶ highlight is runtime-only — hand the pre-highlight displays back to the
    // scoreboard before it is written out with the world.
    applyHighlights(server, Set.of());
    persist();
  }

  private void persist() {
    if (savePath == null) return;
    try {
      Files.createDirectories(savePath.getParent());
      Map<String, String> data = new HashMap<>();
      for (Map.Entry<UUID, String> entry : trackedHolders.entrySet()) {
        data.put(entry.getKey().toString(), entry.getValue());
      }
      try (
        Writer writer = Files.newBufferedWriter(savePath, StandardCharsets.UTF_8)
      ) {
        GSON.toJson(data, writer);
      }
    } catch (Exception e) {
      InitiativeInit.LOGGER.error("Failed to save quest tracking data", e);
    }
  }

  // ---------------------------------------------------------------------------
  // Tick — resolve every 5 ticks, beam fallback every 10
  // ---------------------------------------------------------------------------

  public void tick(MinecraftServer server) {
    tickCounter++;
    if (tickCounter % 5 == 0) {
      refresh(server);
    }
    if (particleFallback && tickCounter % 10 == 0) {
      sendWaypointBeams(server);
    }
  }

  /** The 5-tick pass: validate tracked quests, publish waypoints, paint the sidebar. */
  private void refresh(MinecraftServer server) {
    List<QuestDef> active = activeQuests(server);
    detectQuestCompletions(server, active);
    Set<String> wantHighlighted = new HashSet<>();
    Set<UUID> online = new HashSet<>();

    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      online.add(player.getUUID());
      String holder = trackedHolders.get(player.getUUID());
      if (holder == null) {
        published.remove(player.getUUID());
        continue;
      }
      QuestDef quest = findByHolder(active, holder);
      if (quest == null) {
        // The tracked quest left the sidebar (completed) — auto-untrack.
        trackedHolders.remove(player.getUUID());
        published.remove(player.getUUID());
        persist();
        player.displayClientMessage(
          Component.literal("§7Quest tracking cleared — objective complete."),
          true
        );
        continue;
      }
      published.put(player.getUUID(), resolveWaypoint(player, quest));
      wantHighlighted.add(holder);
    }

    published.keySet().retainAll(online);
    applyHighlights(server, wantHighlighted);
  }

  /**
   * A holder that had a ci_quest score last pass but not this one was completed. Records it
   * in {@link com.thecompanyinc.cobblemoninitiative.data.PlayerProgress#markQuestCompleted}
   * (idempotent — a re-added score can never double-count) and pokes the achievement engine so
   * a quest-count tier can toast live. Seeds silently on the first pass so the quests already
   * active at world load are not mistaken for completions.
   */
  private void detectQuestCompletions(MinecraftServer server, List<QuestDef> active) {
    Set<String> activeHolders = new HashSet<>();
    for (QuestDef quest : active) {
      activeHolders.add(quest.holder);
    }

    if (questLedgerSeeded) {
      List<String> completed = new ArrayList<>();
      for (String holder : lastActiveHolders) {
        if (!activeHolders.contains(holder) && !VOLATILE_QUEST_HOLDERS.contains(holder)) {
          completed.add(holder);
        }
      }
      if (!completed.isEmpty()) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        boolean recorded = false;
        for (ServerPlayer player : players) {
          var progress = InitiativeInit.getProgressManager().getProgress(player);
          for (String holder : completed) {
            if (progress.markQuestCompleted(holder)) recorded = true;
          }
          if (InitiativeInit.getAchievementManager() != null) {
            InitiativeInit.getAchievementManager().evaluateLive(player);
          }
        }
        if (recorded) {
          InitiativeInit.getProgressManager().saveProgress(server);
        }
      }
    } else {
      questLedgerSeeded = true;
    }

    lastActiveHolders = activeHolders;
  }

  // ---------------------------------------------------------------------------
  // Cycling ( ] / [ → next / prev; past the end = tracking off )
  // ---------------------------------------------------------------------------

  /** next (direction=+1) = first active quest if untracked, else the one after the
   *  current in sidebar order, past the last = untrack; prev (-1) is the mirror. */
  public void cycle(ServerPlayer player, int direction) {
    MinecraftServer server = player.getServer();
    if (server == null) return;

    List<QuestDef> active = activeQuests(server);
    String current = trackedHolders.get(player.getUUID());
    QuestDef target = null;
    if (!active.isEmpty()) {
      int index = current == null ? -1 : indexOfHolder(active, current);
      if (index < 0) {
        // Untracked (or stale holder) — enter the list from the direction's end.
        target = direction > 0 ? active.get(0) : active.get(active.size() - 1);
      } else {
        int next = index + direction;
        target = next >= 0 && next < active.size() ? active.get(next) : null;
      }
    }
    setTracked(player, server, target);
  }

  public void clearTracking(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return;
    setTracked(player, server, null);
  }

  private void setTracked(
    ServerPlayer player,
    MinecraftServer server,
    QuestDef quest
  ) {
    if (quest == null) {
      trackedHolders.remove(player.getUUID());
      published.remove(player.getUUID());
      persist();
      refresh(server);
      player.displayClientMessage(
        Component.literal("§7Quest tracking off"),
        true
      );
      return;
    }

    trackedHolders.put(player.getUUID(), quest.holder);
    persist();
    refresh(server); // publish + repaint now — don't wait for the 5-tick pass

    TrackedWaypoint waypoint = published.get(player.getUUID());
    String suffix = waypoint != null && waypoint.hasPos()
      ? ""
      : "§7 (no waypoint for this objective)";
    player.displayClientMessage(
      Component.literal("§bTracking: " + quest.name + suffix),
      true
    );
  }

  /** /cobblemon-initiative track status — the active list with the tracked one marked. */
  public void sendStatus(ServerPlayer player) {
    MinecraftServer server = player.getServer();
    if (server == null) return;

    List<QuestDef> active = activeQuests(server);
    String current = trackedHolders.get(player.getUUID());

    player.sendSystemMessage(Component.literal("§6=== Active Quests ==="));
    if (active.isEmpty()) {
      player.sendSystemMessage(Component.literal("§7(none)"));
    }
    for (QuestDef quest : active) {
      if (quest.holder.equals(current)) {
        player.sendSystemMessage(
          Component.literal("§b▶ " + quest.name + " §7(tracked)")
        );
      } else {
        player.sendSystemMessage(Component.literal("§7  " + quest.name));
      }
    }
    if (current == null) {
      player.sendSystemMessage(
        Component.literal("§7Tracking is off — press ] to start.")
      );
    }
  }

  // ---------------------------------------------------------------------------
  // Active list + stage resolution
  // ---------------------------------------------------------------------------

  /** Registry quests whose holder has a ci_quest score, ordered by score DESC. The
   *  score's PRESENCE is the activity signal — conditions are never re-derived here. */
  private List<QuestDef> activeQuests(MinecraftServer server) {
    Scoreboard scoreboard = server.getScoreboard();
    Objective objective = scoreboard.getObjective(QUEST_OBJECTIVE);
    if (objective == null) return List.of();

    List<QuestDef> active = new ArrayList<>();
    Map<String, Integer> slots = new HashMap<>();
    for (QuestDef quest : quests) {
      ReadOnlyScoreInfo info = scoreboard.getPlayerScoreInfo(
        ScoreHolder.forNameOnly(quest.holder),
        objective
      );
      if (info != null) {
        active.add(quest);
        slots.put(quest.holder, info.value());
      }
    }
    active.sort(
      Comparator.comparingInt((QuestDef q) -> slots.get(q.holder)).reversed()
    );
    return active;
  }

  private static QuestDef findByHolder(List<QuestDef> active, String holder) {
    for (QuestDef quest : active) {
      if (quest.holder.equals(holder)) return quest;
    }
    return null;
  }

  private static int indexOfHolder(List<QuestDef> active, String holder) {
    for (int i = 0; i < active.size(); i++) {
      if (active.get(i).holder.equals(holder)) return i;
    }
    return -1;
  }

  /** FIRST stage whose if_tags are all on the player, none of not_tags are, and all
   *  scores pass. A quest with no matching stage stays tracked, waypointless. */
  private TrackedWaypoint resolveWaypoint(ServerPlayer player, QuestDef quest) {
    StageDef stage = null;
    for (StageDef candidate : quest.stages) {
      if (stageMatches(player, candidate)) {
        stage = candidate;
        break;
      }
    }
    if (stage == null) {
      return new TrackedWaypoint(quest.holder, quest.name, quest.name, false, 0, 0, 0);
    }
    boolean hasPos = stage.x != null && stage.y != null && stage.z != null;
    return new TrackedWaypoint(
      quest.holder,
      quest.name,
      stage.label != null ? stage.label : quest.name,
      hasPos,
      hasPos ? stage.x : 0,
      hasPos ? stage.y : 0,
      hasPos ? stage.z : 0
    );
  }

  private boolean stageMatches(ServerPlayer player, StageDef stage) {
    Set<String> tags = player.getTags();
    for (String tag : stage.ifTags) {
      if (!tags.contains(tag)) return false;
    }
    for (String tag : stage.notTags) {
      if (tags.contains(tag)) return false;
    }
    for (ScoreCheck check : stage.scores) {
      if (!scorePasses(player, check)) return false;
    }
    return true;
  }

  /** Missing objective/score = the condition fails — never an exception. */
  private boolean scorePasses(ServerPlayer player, ScoreCheck check) {
    MinecraftServer server = player.getServer();
    if (server == null || check.objective == null) return false;

    Scoreboard scoreboard = server.getScoreboard();
    Objective objective = scoreboard.getObjective(check.objective);
    if (objective == null) return false;

    // No "holder" key = the PLAYER's score; else a fixed scratch holder (e.g. "#board").
    ScoreHolder holder = check.holder != null
      ? ScoreHolder.forNameOnly(check.holder)
      : player;
    ReadOnlyScoreInfo info = scoreboard.getPlayerScoreInfo(holder, objective);
    if (info == null) return false;

    return switch (check.op == null ? "" : check.op) {
      case "gte" -> info.value() >= check.value;
      case "lte" -> info.value() <= check.value;
      case "eq" -> info.value() == check.value;
      default -> false;
    };
  }

  // ---------------------------------------------------------------------------
  // Sidebar highlight
  // ---------------------------------------------------------------------------

  /** Idempotent both ways: render.mcfunction rewrites lines on its own cadence, so a
   *  repainted line is simply re-saved + re-highlighted on the next pass, and a restore
   *  only fires while the line still carries OUR component. Lines already starting with
   *  "▶ " (q.main ships its own bullet) are left untouched. */
  private void applyHighlights(MinecraftServer server, Set<String> wanted) {
    Scoreboard scoreboard = server.getScoreboard();
    Objective objective = scoreboard.getObjective(QUEST_OBJECTIVE);
    if (objective == null) {
      highlights.clear();
      return;
    }

    Iterator<Map.Entry<String, HighlightState>> it =
      highlights.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, HighlightState> entry = it.next();
      if (wanted.contains(entry.getKey())) continue;
      ScoreHolder holder = ScoreHolder.forNameOnly(entry.getKey());
      // Presence check first — getOrCreate on a reset holder would resurrect a ghost
      // sidebar line (render resets scores when a quest completes).
      if (scoreboard.getPlayerScoreInfo(holder, objective) != null) {
        ScoreAccess access = scoreboard.getOrCreatePlayerScore(holder, objective);
        if (Objects.equals(access.display(), entry.getValue().applied)) {
          access.display(entry.getValue().original);
        }
      }
      it.remove();
    }

    for (String name : wanted) {
      // wanted holders come from activeQuests() this pass — the score exists.
      ScoreAccess access = scoreboard.getOrCreatePlayerScore(
        ScoreHolder.forNameOnly(name),
        objective
      );
      Component display = access.display();
      String literal = display == null ? "" : display.getString();
      if (literal.startsWith(HIGHLIGHT_PREFIX)) continue;
      // Preserve the current component verbatim (some lines carry live macro numbers) —
      // never substitute the registry label into the sidebar.
      Component applied = Component.literal(HIGHLIGHT_PREFIX)
        .withStyle(ChatFormatting.AQUA)
        .append(display != null ? display : Component.literal(name));
      access.display(applied);
      highlights.put(name, new HighlightState(display, applied));
    }
  }

  // ---------------------------------------------------------------------------
  // Particle beam fallback (JourneyMap absent)
  // ---------------------------------------------------------------------------

  private void sendWaypointBeams(MinecraftServer server) {
    ServerLevel overworld = server.overworld();
    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      TrackedWaypoint waypoint = published.get(player.getUUID());
      if (waypoint == null || !waypoint.hasPos()) continue;
      if (player.serverLevel() != overworld) continue;
      // Vertical END_ROD column over the target, ~2 per block, zero speed; force=true
      // so it renders at distance; sent to THAT player only.
      for (int i = 0; i <= BEAM_HEIGHT * 2; i++) {
        overworld.sendParticles(
          player,
          ParticleTypes.END_ROD,
          true,
          waypoint.x(),
          waypoint.y() + i * 0.5,
          waypoint.z(),
          1,
          0.0,
          0.0,
          0.0,
          0.0
        );
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Data model (quest_waypoints.json)
  // ---------------------------------------------------------------------------

  private record HighlightState(Component original, Component applied) {}

  private static class QuestRegistry {

    List<QuestDef> quests = new ArrayList<>();
  }

  private static class QuestDef {

    String holder;
    String name;
    List<StageDef> stages = new ArrayList<>();
  }

  private static class StageDef {

    List<String> ifTags = new ArrayList<>();
    List<String> notTags = new ArrayList<>();
    List<ScoreCheck> scores = new ArrayList<>();
    String label;
    Double x; // null x/y/z = valid but waypointless stage (highlight only)
    Double y;
    Double z;
  }

  private static class ScoreCheck {

    String objective;
    String op; // gte | lte | eq
    int value;
    String holder; // null = the player's own score; e.g. "#board" = scratch holder
  }
}

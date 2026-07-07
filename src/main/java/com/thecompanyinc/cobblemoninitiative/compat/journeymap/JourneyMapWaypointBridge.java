package com.thecompanyinc.cobblemoninitiative.compat.journeymap;

/**
 * Seam between quest tracking and JourneyMap — this class has ZERO journeymap imports
 * (MapFrontiersBridge is the precedent), so it is safe to reference from client code
 * regardless of whether JourneyMap is present.
 *
 * <p>The client poller (QuestTrackClient) pushes the tracked objective here. When
 * JourneyMap is present, its plugin (the only journeymap-importing class, wired solely
 * through the "journeymap" entrypoint) installs a sink that forwards into the JM
 * waypoint API; without JourneyMap the NOOP sink swallows every call (the server-side
 * particle beam fallback covers in-world display).
 */
public final class JourneyMapWaypointBridge {

  /** Receiver side — implemented only by the JourneyMap plugin. */
  public interface Sink {
    void set(String questId, String name, double x, double y, double z, int rgb);

    void clear();
  }

  private static final Sink NOOP = new Sink() {
    @Override
    public void set(String questId, String name, double x, double y, double z, int rgb) {}

    @Override
    public void clear() {}
  };

  private static volatile Sink sink = NOOP;

  private JourneyMapWaypointBridge() {}

  /** Called once, from the JM plugin's initialize(); never from anywhere else. */
  public static void install(Sink installed) {
    sink = installed != null ? installed : NOOP;
  }

  public static void set(String questId, String name, double x, double y, double z, int rgb) {
    sink.set(questId, name, x, y, z, rgb);
  }

  public static void clear() {
    sink.clear();
  }
}

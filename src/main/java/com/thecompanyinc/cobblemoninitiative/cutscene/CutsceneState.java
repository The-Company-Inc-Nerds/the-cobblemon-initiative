package com.thecompanyinc.cobblemoninitiative.cutscene;

import java.util.UUID;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;

/**
 * Per-player mutable state for one playing cutscene. Session-only — never persisted (a live
 * scene is bound to a live spectator camera + rig entity). Mirrors {@code NobleEncounterState}.
 */
public class CutsceneState {

  private final UUID playerId;
  private final String scriptId;
  private final String dimension;
  private final boolean skippable;
  private final boolean hasDouble;

  // ── Restore transform (captured at play start) ───────────────────────────────
  private final double rx, ry, rz;
  private final float ryaw, rpitch;
  private final GameType restoreMode;

  // ── Live handles / playback cursor ───────────────────────────────────────────
  /** The invisible marker armor stand the player's camera is locked to. */
  private ArmorStand rig;
  /** Whole ticks elapsed since play start (drives cue firing). */
  private int tickCount = 0;
  /** Index of the keyframe currently being eased TO. */
  private int keyframeIndex = 0;
  /** Ticks elapsed inside the current keyframe segment. */
  private int ticksIntoKeyframe = 0;
  /** The point the current segment eases FROM (starts at the captured eye transform). */
  private double px, py, pz;
  private float pyaw, ppitch;
  private boolean finished = false;

  // ── Relative-scene base (the captured eye position; keyframe offsets add to this) ─────
  private double baseX, baseY, baseZ;
  private boolean relative = false;
  /** facingFrame scenes: offsets rotate into the captured look direction. */
  private boolean facingFrame = false;
  private float baseYaw, basePitch;

  /** Command run as the player when the scene ends or is skipped (e.g. the engage
   * function that opens the leader battle). Null = none. */
  private String endCommand;

  /** True until the runtime player-skin patch has been applied to the discovered double. */
  private boolean doubleSkinPending = false;

  /** Facing yaw applied to the player-skinned double once discovered (import_new ignores the
   * command-source rotation, so the "you" double is turned by a data-modify in the skin patch).
   * Null = leave the body at its spawned/baked facing. */
  private Float playerDoubleYaw = null;

  /** Chunk + block anchor of the entity-ticking ticket that pins the scene subject (the leader
   * the end-command battles) loaded through the pan. Null when no ticket was added. */
  private net.minecraft.world.level.ChunkPos subjectChunk;
  private net.minecraft.core.BlockPos subjectPos;

  public CutsceneState(UUID playerId, String scriptId, String dimension, boolean skippable,
                       boolean hasDouble, double rx, double ry, double rz, float ryaw,
                       float rpitch, GameType restoreMode, double px, double py, double pz,
                       float pyaw, float ppitch) {
    this.playerId = playerId;
    this.scriptId = scriptId;
    this.dimension = dimension;
    this.skippable = skippable;
    this.hasDouble = hasDouble;
    this.rx = rx; this.ry = ry; this.rz = rz; this.ryaw = ryaw; this.rpitch = rpitch;
    this.restoreMode = restoreMode;
    this.px = px; this.py = py; this.pz = pz; this.pyaw = pyaw; this.ppitch = ppitch;
  }

  public UUID getPlayerId() { return playerId; }
  public String getScriptId() { return scriptId; }
  public String getDimension() { return dimension; }
  public boolean isSkippable() { return skippable; }
  public boolean hasDouble() { return hasDouble; }

  public double getRestoreX() { return rx; }
  public double getRestoreY() { return ry; }
  public double getRestoreZ() { return rz; }
  public float getRestoreYaw() { return ryaw; }
  public float getRestorePitch() { return rpitch; }
  public GameType getRestoreMode() { return restoreMode; }

  public ArmorStand getRig() { return rig; }
  public void setRig(ArmorStand rig) { this.rig = rig; }

  public int getTickCount() { return tickCount; }
  public void incTick() { this.tickCount++; }

  public int getKeyframeIndex() { return keyframeIndex; }
  public int getTicksIntoKeyframe() { return ticksIntoKeyframe; }
  public void incTicksIntoKeyframe() { this.ticksIntoKeyframe++; }

  /** Advance to the next segment, anchoring the new "from" point at the keyframe just reached. */
  public void advanceKeyframe(double x, double y, double z, float yaw, float pitch) {
    this.keyframeIndex++;
    this.ticksIntoKeyframe = 0;
    this.px = x; this.py = y; this.pz = z; this.pyaw = yaw; this.ppitch = pitch;
  }

  public double getPrevX() { return px; }
  public double getPrevY() { return py; }
  public double getPrevZ() { return pz; }
  public float getPrevYaw() { return pyaw; }
  public float getPrevPitch() { return ppitch; }

  public boolean isFinished() { return finished; }
  public void setFinished(boolean v) { this.finished = v; }

  public void setBase(double x, double y, double z, boolean relative) {
    this.baseX = x; this.baseY = y; this.baseZ = z; this.relative = relative;
  }
  public void setFacingFrame(float yaw, float pitch) {
    this.facingFrame = true; this.baseYaw = yaw; this.basePitch = pitch;
  }
  public double getBaseX() { return baseX; }
  public double getBaseY() { return baseY; }
  public double getBaseZ() { return baseZ; }
  public boolean isRelative() { return relative; }
  public boolean isFacingFrame() { return facingFrame; }
  public float getBaseYaw() { return baseYaw; }
  public float getBasePitch() { return basePitch; }

  public String getEndCommand() { return endCommand; }
  public void setEndCommand(String cmd) { this.endCommand = cmd; }

  public boolean isDoubleSkinPending() { return doubleSkinPending; }
  public void setDoubleSkinPending(boolean v) { this.doubleSkinPending = v; }

  public Float getPlayerDoubleYaw() { return playerDoubleYaw; }
  public void setPlayerDoubleYaw(float yaw) { this.playerDoubleYaw = yaw; }

  public void setSubjectAnchor(net.minecraft.world.level.ChunkPos c, net.minecraft.core.BlockPos p) {
    this.subjectChunk = c; this.subjectPos = p;
  }
  public net.minecraft.world.level.ChunkPos getSubjectChunk() { return subjectChunk; }
  public net.minecraft.core.BlockPos getSubjectPos() { return subjectPos; }
}

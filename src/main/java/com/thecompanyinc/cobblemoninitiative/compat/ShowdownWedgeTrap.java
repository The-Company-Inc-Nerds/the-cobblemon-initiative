package com.thecompanyinc.cobblemoninitiative.compat;

import com.thecompanyinc.cobblemoninitiative.InitiativeInit;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Capture-and-contain for the showdown-engine wedge (root-caused 2026-07-17): a Java
 * exception unwinding out of {@code ShowdownInterpreter.interpretMessage} back through the
 * Graal polyglot boundary poisons the JS battle stream — every later battle freezes
 * post-send-out (actors mustChoose=false/request=true forever) until a full
 * {@code dev showdown revive}. The FancyMenu NaN kill is the same crash family.
 *
 * <p>{@link com.thecompanyinc.cobblemoninitiative.mixin.GraalShowdownServiceMixin} wraps the
 * JS→Java callback and routes any throwable here instead of letting it unwind into the
 * engine loop. Containment beats propagation on every axis: the wedge previously killed
 * ALL subsequent battles; a swallowed message at worst stalls the ONE battle whose state
 * machine missed it (recoverable with /stopbattle — no context rebuild needed).
 *
 * <p>Each trapped fault is the exact evidence the TODO's "find the organic wedge TRIGGER"
 * item wants: the battle id, the raw showdown message being interpreted, and the full Java
 * stack. Read back with {@code /cobblemon-initiative dev showdown trap}.
 */
public final class ShowdownWedgeTrap {

  /** One contained fault: what was being interpreted and what blew up. */
  public record TrapReport(int ordinal, String battleId, String message, String stack) {}

  private static final int MAX_REPORTS = 5;
  private static final Deque<TrapReport> reports = new ArrayDeque<>();
  private static int totalTrapped = 0;
  private static volatile boolean pendingNotify = false;

  private ShowdownWedgeTrap() {}

  /** Called from the mixin (showdown thread) — log, store, and flag the toast. */
  public static synchronized void record(String battleId, String message, Throwable t) {
    totalTrapped++;
    StringWriter stack = new StringWriter();
    t.printStackTrace(new PrintWriter(stack));
    if (reports.size() >= MAX_REPORTS) {
      reports.removeFirst();
    }
    reports.addLast(new TrapReport(totalTrapped, battleId, message, stack.toString()));
    pendingNotify = true;

    InitiativeInit.LOGGER.error(
      "[ShowdownWedgeTrap] Contained a Java exception mid-interpretMessage "
        + "(fault #{} — this is the wedge trigger the 2026-07-17 bisect was hunting).\n"
        + "  battleId: {}\n  message: {}\n  cause:",
      totalTrapped, battleId, message, t);
  }

  /** Server-tick consumer for the one-time player toast (set from the showdown thread). */
  public static boolean consumePendingNotify() {
    if (!pendingNotify) return false;
    pendingNotify = false;
    return true;
  }

  public static synchronized int totalTrapped() {
    return totalTrapped;
  }

  public static synchronized List<TrapReport> reportsSnapshot() {
    return new ArrayList<>(reports);
  }
}

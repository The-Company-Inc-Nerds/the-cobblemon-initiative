package com.thecompanyinc.cobblemoninitiative.achievement;

import java.util.function.Predicate;

/**
 * One derived-state ("global") achievement in the manifest.
 *
 * @param id          short id mirrored into {@code PlayerProgress.earnedAchievements} — the
 *                    grant-once dedup key and the persisted record that a batch already
 *                    backfilled this world. Must not collide with a levelcaps.json id.
 * @param advancement full advancement path (e.g. {@code cobblemon_initiative:frontier/hall_tower}),
 *                    granted via command; the JSON ships {@code show_toast:false} so the grant
 *                    itself is silent and the live/backfill split is decided purely in Java.
 * @param title       display title (chat line + overlay + toast fallback)
 * @param description one-line description (chat + overlay)
 * @param icon        display item id (sent to the overlay; the client toast uses the JSON icon)
 * @param qualifies   predicate over the derived-state snapshot; true = the player has earned it
 */
public record GlobalAchievement(
  String id,
  String advancement,
  String title,
  String description,
  String icon,
  Predicate<AchievementContext> qualifies
) {}

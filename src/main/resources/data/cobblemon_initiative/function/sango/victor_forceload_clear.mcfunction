# Release the reveal-site chunk force-loaded by victor_transform. Scheduled 15s after the
# transform so the Victini NPC and its cutscene have fully settled before the chunk is
# allowed to unload again (the NPC persists in the save; the tick-14 despawn-on-
# victini_joined path re-loads it as needed via `at @e[...]`).
forceload remove 2536 2900

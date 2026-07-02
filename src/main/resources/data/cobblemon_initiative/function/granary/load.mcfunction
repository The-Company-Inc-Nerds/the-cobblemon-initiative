# Granary objectives. Run once on datapack load (wired via minecraft:load tag).
# granary_ambush_armed: per-player latch. Set to 1 by the Granary hostile-tier trade
#   button (the keeper trades anyway, then files you); a post-trade poller reads it to
#   fire the ambush battle — same pattern as wheat_ambush_armed (poller lands with the
#   villain trainer authoring, TODO P4/P5). Add fails silently if it already exists.
scoreboard objectives add granary_ambush_armed dummy

# Wheat-trader recognition/ambush objectives. Run once on datapack load.
# fields_liberated: SHARED counter owned by the Field Liberation designer.
#   Incremented when a set-piece occupied wheat field is liberated.
#   Declared here defensively (add fails silently if it already exists).
scoreboard objectives add fields_liberated dummy
# wheat_ambush_armed: per-player latch. Set to 1 by the trade dialog the moment a
#   recognition/ambush-tier trader opens its trade screen; the tick poller reads it
#   to detect "a trade window was opened" and arms the post-trade battle.
scoreboard objectives add wheat_ambush_armed dummy

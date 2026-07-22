# companion_deepcore_timburr — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_deepcore_timburr — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_deepcore_timburr to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_deepcore_timburr]
scoreboard players set #amb_deepcore_timburr ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_deepcore_timburr.npc.snbt 1113.5 97 3200.5

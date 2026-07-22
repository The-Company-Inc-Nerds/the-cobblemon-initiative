# companion_ironwave_wingull — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_ironwave_wingull — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_ironwave_wingull to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_ironwave_wingull]
scoreboard players set #amb_ironwave_wingull ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_ironwave_wingull.npc.snbt 3534.5 169 2761.5

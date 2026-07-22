# companion_route_joltik — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_route_joltik — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_route_joltik to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_route_joltik]
scoreboard players set #amb_route_joltik ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_route_joltik.npc.snbt 1428.5 90 954.5

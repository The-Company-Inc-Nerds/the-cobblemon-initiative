# companion_ludwig — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_ludwig — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_ludwig to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_ludwig]
scoreboard players set #amb_ludwig ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_ludwig.npc.snbt 1537.8 102.1 2114.6

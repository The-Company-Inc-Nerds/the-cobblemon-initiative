# companion_wooloo — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_wooloo — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_wooloo to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_wooloo]
scoreboard players set #amb_wooloo ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_wooloo.npc.snbt 1513.5 84 1988.5

# companion_takehara_sewaddle — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_takehara_sewaddle — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_takehara_sewaddle to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_takehara_sewaddle]
scoreboard players set #amb_takehara_sewaddle ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_takehara_sewaddle.npc.snbt 1817.5 115 2558.5

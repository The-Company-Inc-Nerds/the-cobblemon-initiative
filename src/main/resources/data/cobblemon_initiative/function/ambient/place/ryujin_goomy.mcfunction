# companion_ryujin_goomy — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_ryujin_goomy — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_ryujin_goomy to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_ryujin_goomy]
scoreboard players set #amb_ryujin_goomy ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_ryujin_goomy.npc.snbt 2111.5 203 868.5

# companion_lopunny_right — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_lopunny_right — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_lopunny_right to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_lopunny_right]
scoreboard players set #amb_lopunny_right ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_lopunny_right.npc.snbt 1612.9 197.1 1101.6

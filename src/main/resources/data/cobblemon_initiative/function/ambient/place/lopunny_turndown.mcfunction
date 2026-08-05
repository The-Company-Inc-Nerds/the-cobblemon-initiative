# companion_lopunny_turndown — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_lopunny_turndown — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_lopunny_turndown to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_lopunny_turndown]
scoreboard players set #amb_lopunny_turndown ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_lopunny_turndown.npc.snbt 1615.9 197.5 1099.8

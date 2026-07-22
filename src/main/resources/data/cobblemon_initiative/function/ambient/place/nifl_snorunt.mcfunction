# companion_nifl_snorunt — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_nifl_snorunt — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_nifl_snorunt to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_nifl_snorunt]
scoreboard players set #amb_nifl_snorunt ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_nifl_snorunt.npc.snbt 3607.5 120 1906.5

# companion_gaviota_buizel — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_gaviota_buizel — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_gaviota_buizel to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_gaviota_buizel]
scoreboard players set #amb_gaviota_buizel ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_gaviota_buizel.npc.snbt 421.5 67 3554.5

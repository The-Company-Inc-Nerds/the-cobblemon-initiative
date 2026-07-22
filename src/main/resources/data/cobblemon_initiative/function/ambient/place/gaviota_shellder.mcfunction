# companion_gaviota_shellder — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_gaviota_shellder — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_gaviota_shellder to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_gaviota_shellder]
scoreboard players set #amb_gaviota_shellder ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_gaviota_shellder.npc.snbt 607.5 88 3588.5

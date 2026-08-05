# companion_incineroar_bed — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_incineroar_bed — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_incineroar_bed to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_incineroar_bed]
scoreboard players set #amb_incineroar_bed ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_incineroar_bed.npc.snbt 1628.2 197 1116.2

# companion_hua_zhan_deerling — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_hua_zhan_deerling — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_hua_zhan_deerling to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_hua_zhan_deerling]
scoreboard players set #amb_hua_zhan_deerling ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_hua_zhan_deerling.npc.snbt 1475.5 85 2020.5

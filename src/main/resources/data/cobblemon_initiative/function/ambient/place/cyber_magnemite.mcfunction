# companion_cyber_magnemite — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_cyber_magnemite — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_cyber_magnemite to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_cyber_magnemite]
scoreboard players set #amb_cyber_magnemite ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_cyber_magnemite.npc.snbt 1479.5 91 1318.5

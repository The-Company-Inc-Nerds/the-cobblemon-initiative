# companion_royal_town_taillow — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_royal_town_taillow — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_royal_town_taillow to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_royal_town_taillow]
scoreboard players set #amb_royal_town_taillow ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_royal_town_taillow.npc.snbt 3511.5 212 2938.5

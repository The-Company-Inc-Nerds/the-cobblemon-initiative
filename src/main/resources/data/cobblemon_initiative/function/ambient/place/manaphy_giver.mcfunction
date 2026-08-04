# manaphy_giver — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_manaphy_giver — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_manaphy_giver to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_manaphy_giver]
scoreboard players set #amb_manaphy_giver ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/manaphy_giver.npc.snbt 2767.9 33 3490.5

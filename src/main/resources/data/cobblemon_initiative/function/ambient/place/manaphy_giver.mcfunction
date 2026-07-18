# manaphy_giver — latch FIRST; reset #amb_manaphy_giver to 0 (+ kill the body) to respawn.
scoreboard players set #amb_manaphy_giver ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/manaphy_giver.npc.snbt 2760.5 33 3490.5

# r12_lorekeeper_pylon — latch FIRST; reset #amb_r12_lorekeeper_pylon to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r12_lorekeeper_pylon ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r12_lorekeeper_pylon.npc.snbt 1525.5 64 1698.5

# cyber_grid_broker — latch FIRST; reset #amb_cyber_grid_broker to 0 (+ kill the body) to respawn.
scoreboard players set #amb_cyber_grid_broker ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/cyber_grid_broker.npc.snbt 1555.5 65 1108.5

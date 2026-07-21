# ground_shrine_leader — latch FIRST; reset #amb_ground_shrine_leader to 0 (+ kill the body) to respawn.
scoreboard players set #amb_ground_shrine_leader ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/ground_shrine_leader.npc.snbt 1901.6 81 4073.2

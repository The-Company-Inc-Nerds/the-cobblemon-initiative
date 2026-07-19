# ground_shrine_leader — latch FIRST; reset #amb_ground_shrine_leader to 0 (+ kill the body) to respawn.
scoreboard players set #amb_ground_shrine_leader ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/ground_shrine_leader.npc.snbt 1910.5 89 4049.5

# fire_shrine_leader — latch FIRST; reset #amb_fire_shrine_leader to 0 (+ kill the body) to respawn.
scoreboard players set #amb_fire_shrine_leader ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/fire_shrine_leader.npc.snbt 3510.5 51 4702.5

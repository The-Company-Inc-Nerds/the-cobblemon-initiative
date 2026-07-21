# ice_shrine_leader — latch FIRST; reset #amb_ice_shrine_leader to 0 (+ kill the body) to respawn.
scoreboard players set #amb_ice_shrine_leader ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/ice_shrine_leader.npc.snbt 3618.6 65 1937.3

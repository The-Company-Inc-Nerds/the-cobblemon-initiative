# fairy_shrine_leader — latch FIRST; reset #amb_fairy_shrine_leader to 0 (+ kill the body) to respawn.
scoreboard players set #amb_fairy_shrine_leader ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/fairy_shrine_leader.npc.snbt 951.5 3 2715.5

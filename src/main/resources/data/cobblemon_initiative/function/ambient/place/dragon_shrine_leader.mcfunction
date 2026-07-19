# dragon_shrine_leader — latch FIRST; reset #amb_dragon_shrine_leader to 0 (+ kill the body) to respawn.
scoreboard players set #amb_dragon_shrine_leader ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/dragon_shrine_leader.npc.snbt 2004.5 71 919.5

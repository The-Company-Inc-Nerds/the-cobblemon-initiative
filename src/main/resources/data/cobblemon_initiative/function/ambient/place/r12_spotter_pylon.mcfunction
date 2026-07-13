# r12_spotter_pylon — latch FIRST; reset #amb_r12_spotter_pylon to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r12_spotter_pylon ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r12_spotter_pylon.npc.snbt 1520.5 64 1710.5

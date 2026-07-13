# r16_spotter_causeway — latch FIRST; reset #amb_r16_spotter_causeway to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r16_spotter_causeway ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r16_spotter_causeway.npc.snbt 3681.5 64 3003.5

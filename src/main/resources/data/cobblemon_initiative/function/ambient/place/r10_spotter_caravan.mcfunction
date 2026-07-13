# r10_spotter_caravan — latch FIRST; reset #amb_r10_spotter_caravan to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r10_spotter_caravan ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r10_spotter_caravan.npc.snbt 2235.5 64 3510.5

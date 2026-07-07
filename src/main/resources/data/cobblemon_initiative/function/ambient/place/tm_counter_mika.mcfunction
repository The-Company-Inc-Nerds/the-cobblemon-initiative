# tm_counter_mika — latch FIRST; reset #amb_tm_counter_mika to 0 (+ kill the body) to respawn.
scoreboard players set #amb_tm_counter_mika ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/tm_counter_mika.npc.snbt 1904.5 113 2606.5

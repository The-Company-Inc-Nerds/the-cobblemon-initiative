# mm_field_guard — latch FIRST; reset #amb_mm_field_guard to 0 (+ kill the body) to respawn.
scoreboard players set #amb_mm_field_guard ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/mm_field_guard.npc.snbt 1229.5 64 2820.5

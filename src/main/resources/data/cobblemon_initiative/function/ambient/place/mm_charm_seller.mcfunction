# mm_charm_seller — latch FIRST; reset #amb_mm_charm_seller to 0 (+ kill the body) to respawn.
scoreboard players set #amb_mm_charm_seller ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/mm_charm_seller.npc.snbt 1076.5 64 2452.5

# ryujin_keeper_hana — latch FIRST; reset #amb_ryujin_keeper_hana to 0 (+ kill the body) to respawn.
scoreboard players set #amb_ryujin_keeper_hana ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/ryujin_keeper_hana.npc.snbt 2146.5 64 900.5

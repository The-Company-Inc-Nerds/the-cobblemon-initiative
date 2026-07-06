# villain_site_manager — latch FIRST; reset #amb_villain_site_manager to 0 (+ kill the body) to respawn.
scoreboard players set #amb_villain_site_manager ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/villain_site_manager.npc.snbt 1603.5 89 2488.5

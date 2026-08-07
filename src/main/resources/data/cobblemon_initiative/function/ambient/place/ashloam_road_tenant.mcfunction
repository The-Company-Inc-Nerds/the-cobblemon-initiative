# ashloam_road_tenant — latch FIRST; reset #amb_ashloam_road_tenant to 0 (+ kill the body) to respawn.
scoreboard players set #amb_ashloam_road_tenant ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/ashloam_road_tenant.npc.snbt 3290.3 104.6 3930.7

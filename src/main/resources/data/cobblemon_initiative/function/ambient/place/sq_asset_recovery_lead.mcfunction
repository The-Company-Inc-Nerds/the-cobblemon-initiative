# sq_asset_recovery_lead — latch FIRST; reset #amb_sq_asset_recovery_lead to 0 (+ kill the body) to respawn.
scoreboard players set #amb_sq_asset_recovery_lead ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/sq_asset_recovery_lead.npc.snbt 3620.5 66 4658.5

# repairs wave a26 — apply: 0.7.0-alpha.10 playtest wave. Chunks forceloaded by the arm.
# Apprentice Faye — re-latch the 0.9-block nudge at the still pool. limit=1,sort=nearest so the
# kill takes only Faye (she stands at the old spot); no other gym body sits within r2.
execute positioned 922.4 92.0 2439.5 run kill @e[type=easy_npc:humanoid,distance=..2,limit=1,sort=nearest]
scoreboard players set #amb_mystic_apprentice ci_ambient 0
forceload remove 916 2434 928 2446

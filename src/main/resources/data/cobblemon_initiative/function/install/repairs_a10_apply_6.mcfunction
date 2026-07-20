# repairs wave a10 — apply 6/6: kill stale bodies, re-arm latches.
execute positioned 1180.5 123 3260.5 run kill @e[type=easy_npc:humanoid,name="Corliss",distance=..48]
scoreboard players set #amb_wheat_trader_deepcore ci_ambient 0

forceload remove 1152 3232
forceload remove 1152 3248
forceload remove 1152 3264
forceload remove 1168 3232
forceload remove 1168 3248
forceload remove 1168 3264
forceload remove 1184 3232
forceload remove 1184 3248
forceload remove 1184 3264
tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a10: dialog pass — 171 locals re-latch with fresher things to say.","color":"gray"}]

# Tenants of Record — WALK HOME accepted (round 13c: the family genuinely FOLLOWS).
# Run as @s = the player from Old Dengs walk button (ExecAsUser; commands stay bare).
# Verified 6.25.0 grammar: easy_npc objective <selector> set follow player <player> —
# Prio 1, speed 0.7, engage 2-16 blocks, native teleport catch-up past 12.
tag @s add homecoming_walking
easy_npc objective @e[tag=deng_old,limit=1] set follow player @s
easy_npc objective @e[tag=deng_granny,limit=1] set follow player @s
easy_npc objective @e[tag=deng_haoran,limit=1] set follow player @s
scoreboard players set #deng_walk ci_ambient 0
tellraw @s {"text":"Old Deng banks the fire with two hands and no ceremony. The family falls in behind you.","color":"gold"}
title @s actionbar {"text":"Lead the Dengs east to the Firstfurrow gate.","color":"gold"}

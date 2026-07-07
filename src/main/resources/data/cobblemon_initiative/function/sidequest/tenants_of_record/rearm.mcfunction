# Escort follow re-arm (see walk_tick). Run as/at the walking player.
scoreboard players set #deng_walk ci_ambient 0
easy_npc objective @e[tag=deng_old,limit=1] set follow player @s
easy_npc objective @e[tag=deng_granny,limit=1] set follow player @s
easy_npc objective @e[tag=deng_haoran,limit=1] set follow player @s

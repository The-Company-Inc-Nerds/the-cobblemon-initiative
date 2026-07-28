# Minutes of the Quarterly Review — NOTICED at the landing. Run as/at the player from the
# near_office seen-branch. The staff escort the eavesdropper out of the branch office.
# Cooldown ci_kick_cd (200t = 10 s) so re-entry is not an instant tp loop.
scoreboard players reset @s ci_loiter_hz
scoreboard players set @s ci_kick_cd 200
tellraw @s [{"text":"A hand closes on your shoulder. ","color":"gray","italic":true},{"text":"Floor privileges are assigned, not assumed — and yours were never assigned.","color":"gray"}]
title @s actionbar [{"text":"ESCORTED OUT","color":"red","bold":true},{"text":" — the Company appreciates your discretion.","color":"gray"}]
playsound minecraft:entity.villager.no player @s ~ ~ ~ 1 0.8
tp @s 1528.7 86.0 2001.0 facing 1535.5 86.0 1996.5
execute at @s run playsound minecraft:block.wooden_door.close player @s ~ ~ ~ 1 0.9
execute at @s run particle minecraft:poof ~ ~1 ~ 0.3 0.5 0.3 0.02 20 force @s

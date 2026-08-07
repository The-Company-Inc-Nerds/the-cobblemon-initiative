# Trident Tide-Race — start an attempt. Run AS the player (dialog button, as_player):
#   function cobblemon_initiative:sidequest/trident_race/start {ticks:600}   (30 s clock)
# Guard: ignore re-clicks while a run is live (no timer-reset exploit).
execute if entity @s[tag=ci_trident_racing] run return 0
# One-time race (playtest 2026-08-06 N1): once cleared, it never runs again.
execute if entity @s[tag=sq_trident_race_done] run return 0
# Grant the Tide-Caller's Trident (Riptide III, unbreakable) and arm the ring counter. Riptide needs
# the player to be IN water/rain to fire — the whole course is submerged so the dash keeps launching.
loot give @s loot cobblemon_initiative:npc_gift/riptide_trident
scoreboard players set @s ci_trace_cp 0
$scoreboard players set #time ci_trace $(ticks)
scoreboard players operation #secs ci_trace = #time ci_trace
scoreboard players operation #secs ci_trace /= #twenty ci_trace
execute store result bossbar cobblemon_initiative:trident_race max run scoreboard players get #secs ci_trace
execute store result bossbar cobblemon_initiative:trident_race value run scoreboard players get #secs ci_trace
tag @s add ci_trident_racing
bossbar set cobblemon_initiative:trident_race players @a
bossbar set cobblemon_initiative:trident_race visible true
title @s title [{"text":"THE TIDE-RING RACE","color":"aqua","bold":true}]
title @s subtitle [{"text":"Riptide the rings before the tide turns","color":"gray"}]
execute at @s run playsound minecraft:item.trident.riptide_1 player @s ~ ~ ~ 1 1.2

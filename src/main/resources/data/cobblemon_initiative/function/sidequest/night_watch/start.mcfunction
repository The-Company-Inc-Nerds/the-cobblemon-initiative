# First Night Watch — light the lantern. Run AS THE PLAYER (dialog button, as_player).
# The button is always visible post-liberation; the DUSK GATE lives here: by day the
# start refuses politely via actionbar (spec) and arms only at 12000..23999 daytime.
execute if entity @s[tag=ci_watching] run return 0
execute store result score #t ci_watch run time query daytime
execute unless score #t ci_watch matches 12000..23999 run title @s actionbar [{"text":"The watch begins at dusk","color":"gray"}]
execute unless score #t ci_watch matches 12000..23999 run playsound minecraft:block.note_block.hat master @s ~ ~ ~ 1 0.7
execute unless score #t ci_watch matches 12000..23999 run return 0
# Arm the watch: reset the cull ledger, tag in, size the bossbar to the remaining night.
scoreboard players set @s nw_z 0
scoreboard players set @s nw_k 0
scoreboard players set @s nw_p 0
scoreboard players set @s nw_c 0
scoreboard players set @s nw_total 0
scoreboard players set @s nw_grace 0
tag @s add ci_watching
# Fixed watch length (2400 ticks ≈ 2 min) — shorter than the old dusk→dawn hold, and paired
# with scripted spawn pulses in run_tick so the field stays busy. nw_ticks counts up to the cap.
scoreboard players set @s nw_ticks 0
scoreboard players set @s nw_spawn 0
bossbar set cobblemon_initiative:night_watch max 2400
bossbar set cobblemon_initiative:night_watch value 2400
bossbar set cobblemon_initiative:night_watch players @s
bossbar set cobblemon_initiative:night_watch visible true
title @s title [{"text":"STAND THE WATCH","color":"red","bold":true}]
title @s subtitle [{"text":"Hold the field until first light","color":"gray"}]
playsound minecraft:block.bell.use master @s ~ ~ ~ 1 0.8

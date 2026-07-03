# Sango Classic — one-second countdown loop (server context via schedule; shrine-parkour cadence).
scoreboard players remove #time ci_classic 1
execute store result bossbar cobblemon_initiative:sango_classic value run scoreboard players get #time ci_classic
execute if score #time ci_classic matches 60 run title @a[tag=classic_active] actionbar [{"text":"One minute left in the quarter","color":"yellow"}]
execute if score #time ci_classic matches 30 run title @a[tag=classic_active] actionbar [{"text":"Thirty seconds","color":"gold"}]
execute if score #time ci_classic matches 10 run title @a[tag=classic_active] actionbar [{"text":"Ten seconds — reel it in","color":"red"}]
execute if score #on ci_classic matches 1 if score #time ci_classic matches 1.. run schedule function cobblemon_initiative:sidequest/derby/second 1s
execute if score #on ci_classic matches 1 if score #time ci_classic matches ..0 run function cobblemon_initiative:sidequest/derby/fail

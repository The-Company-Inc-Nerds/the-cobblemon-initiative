# Gaviota Open (alpha.26 audit rulings) — one-second countdown loop (server context via
# schedule; Sango derby/second clone, thresholds scaled to the 300-second record round).
scoreboard players remove #time ci_open 1
execute store result bossbar cobblemon_initiative:gaviota_open value run scoreboard players get #time ci_open
execute if score #time ci_open matches 120 run title @a[tag=gaviota_open_active] actionbar [{"text":"Two minutes left on the clock","color":"yellow"}]
execute if score #time ci_open matches 60 run title @a[tag=gaviota_open_active] actionbar [{"text":"One minute","color":"yellow"}]
execute if score #time ci_open matches 30 run title @a[tag=gaviota_open_active] actionbar [{"text":"Thirty seconds","color":"gold"}]
execute if score #time ci_open matches 10 run title @a[tag=gaviota_open_active] actionbar [{"text":"Ten seconds — reel it in","color":"red"}]
execute if score #on ci_open matches 1 if score #time ci_open matches 1.. run schedule function cobblemon_initiative:sidequest/gaviota_open/second 1s
execute if score #on ci_open matches 1 if score #time ci_open matches ..0 run function cobblemon_initiative:sidequest/gaviota_open/fail

# Sango Classic derby (Record Quarter) — objectives + bossbar setup.
# NEEDS LOAD REGISTRATION: the orchestrator must add this function to the load chain
# (reported under loadFunctions; do not edit function tags from quest agents).
# Idempotent and relog-safe: objectives add no-ops when present, #init guards the bossbar,
# and a derby that was live at shutdown re-arms its schedule loop here.
scoreboard objectives add ci_classic dummy
scoreboard objectives add ci_fish_cod dummy
scoreboard objectives add ci_fish_salmon dummy
scoreboard objectives add ci_fish_puffer dummy
scoreboard objectives add ci_fish_tropical dummy
scoreboard objectives add ci_fish_total dummy
scoreboard objectives add ci_fish_rem dummy
scoreboard objectives add ci_fish_take dummy
scoreboard objectives add ci_fish_base_cod dummy
scoreboard objectives add ci_fish_base_salmon dummy
scoreboard objectives add ci_fish_base_puffer dummy
scoreboard objectives add ci_fish_base_tropical dummy
scoreboard objectives add ci_classic_win dummy
scoreboard objectives add ci_classic_bonus dummy
execute unless score #init ci_classic matches 1 run bossbar add cobblemon_initiative:sango_classic [{"text":"THE SANGO CLASSIC","color":"aqua","bold":true}]
scoreboard players set #init ci_classic 1
bossbar set cobblemon_initiative:sango_classic color blue
bossbar set cobblemon_initiative:sango_classic style notched_10
bossbar set cobblemon_initiative:sango_classic max 95
execute unless score #on ci_classic matches -2147483648..2147483647 run scoreboard players set #on ci_classic 0
# Resume the countdown if a derby was live when the server stopped.
execute if score #on ci_classic matches 1 run schedule function cobblemon_initiative:sidequest/derby/second 1s

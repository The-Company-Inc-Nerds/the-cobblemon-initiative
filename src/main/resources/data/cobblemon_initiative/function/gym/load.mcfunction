# Gym ladder objectives. Registered in #minecraft:load (tags/function/load.json).
# <gym>_tower — per-player count of defeated gym trainers for that gym
# (maintained every tick by gym/<gym>_tower; the compiler lowers each gym's dialog
# score gates {score: <gym>_tower gte 1|2|4} to band tags <gym>_tower_gte_1/2/4,
# which weaken that gym's Jr. Apprentice / Apprentice / Leader respectively).
scoreboard objectives add takehara_tower dummy
# ci_gym — shared gym-mechanics scratch scores (Cicada hover lift latch + Y tracking).
scoreboard objectives add ci_gym dummy
# Deepcore dojo PVP latches (a22): floor masters #dc_floor_1..4 (0 armed / 1 hostile live /
# 2 cleared) + the pit #dc_pit_stage (0 none / 1 Striker live / 2 Ken live / 3 cleared).
# Init-if-unset (orc/load idiom) — a live/cleared value survives relog, never re-arms progress.
execute unless score #dc_floor_1 ci_gym matches 0.. run scoreboard players set #dc_floor_1 ci_gym 0
execute unless score #dc_floor_2 ci_gym matches 0.. run scoreboard players set #dc_floor_2 ci_gym 0
execute unless score #dc_floor_3 ci_gym matches 0.. run scoreboard players set #dc_floor_3 ci_gym 0
execute unless score #dc_floor_4 ci_gym matches 0.. run scoreboard players set #dc_floor_4 ci_gym 0
execute unless score #dc_pit_stage ci_gym matches 0.. run scoreboard players set #dc_pit_stage ci_gym 0
# nifl_wo — whiteout-approach taunt cooldown (per-player, gym/nifl_whiteout — off_record obs_cd precedent).
scoreboard objectives add nifl_wo dummy
# scorchspire_heat/_away — Banked Coals heat gauge + away-cooldown (gym/scorchspire_heat tick fn).
scoreboard objectives add scorchspire_heat dummy
scoreboard objectives add scorchspire_away dummy
scoreboard objectives add mystic_tower dummy
scoreboard objectives add deepcore_tower dummy
scoreboard objectives add gaviota_tower dummy
scoreboard objectives add kalahar_tower dummy
scoreboard objectives add cyber_tower dummy
scoreboard objectives add ryujin_tower dummy
scoreboard objectives add nifl_tower dummy
scoreboard objectives add scorchspire_tower dummy

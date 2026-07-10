# Gym ladder objectives. Registered in #minecraft:load (tags/function/load.json).
# <gym>_tower — per-player count of defeated gym trainers for that gym
# (maintained every tick by gym/<gym>_tower; the compiler lowers each gym's dialog
# score gates {score: <gym>_tower gte 1|2|4} to band tags <gym>_tower_gte_1/2/4,
# which weaken that gym's Jr. Apprentice / Apprentice / Leader respectively).
scoreboard objectives add takehara_tower dummy
scoreboard objectives add mystic_tower dummy
scoreboard objectives add deepcore_tower dummy
scoreboard objectives add gaviota_tower dummy
scoreboard objectives add kalahar_tower dummy
scoreboard objectives add cyber_tower dummy
scoreboard objectives add ryujin_tower dummy
scoreboard objectives add nifl_tower dummy
scoreboard objectives add scorchspire_tower dummy

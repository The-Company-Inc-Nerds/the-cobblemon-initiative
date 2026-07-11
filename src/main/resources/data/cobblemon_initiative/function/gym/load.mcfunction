# Gym ladder objectives. Registered in #minecraft:load (tags/function/load.json).
# <gym>_tower — per-player count of defeated gym trainers for that gym
# (maintained every tick by gym/<gym>_tower; the compiler lowers each gym's dialog
# score gates {score: <gym>_tower gte 1|2|4} to band tags <gym>_tower_gte_1/2/4,
# which weaken that gym's Jr. Apprentice / Apprentice / Leader respectively).
scoreboard objectives add takehara_tower dummy
# ci_gym — shared gym-mechanics scratch scores (Cicada hover lift latch + Y tracking).
scoreboard objectives add ci_gym dummy
# gaviota_tide — the port gym's world tide clock (gym/gaviota_tide tick fn).
scoreboard objectives add gaviota_tide dummy
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

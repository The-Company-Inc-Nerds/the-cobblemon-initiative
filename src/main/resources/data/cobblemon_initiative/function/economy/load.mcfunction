# Wheat War economy — declare the CobbleDollar instability index + scratch/const objectives.
# Registered in #minecraft:load. Idempotent: 'objectives add' no-ops if it exists, and
# cd_instability is only seeded to 0 when UNSET, so it persists across relogs (world data).
scoreboard objectives add cd_instability dummy
scoreboard objectives add cd_calc dummy
scoreboard objectives add cd_const dummy
scoreboard players set #two cd_const 2
scoreboard players set #four cd_const 4
scoreboard players set #hundred cd_const 100
execute unless score #idx cd_instability matches -2147483648..2147483647 run scoreboard players set #idx cd_instability 0
# Dawn systems (review B6): ci_dawn = the economy/dawn day-latch scratch; hz_market =
# the rolled East Market Street day (#ware_day holder, mirrored onto players every tick
# so the compiler band tags hz_market_eq_1/2 can gate trader ware buttons).
scoreboard objectives add ci_dawn dummy
scoreboard objectives add hz_market dummy
# Battle Frontier hall passes — a soft/flavor CobbleDollars sink (granted on registration and
# by frontier/buy_pass; never consumed, never gates a battle). World-data dummy, persists.
scoreboard objectives add frontier_passes dummy


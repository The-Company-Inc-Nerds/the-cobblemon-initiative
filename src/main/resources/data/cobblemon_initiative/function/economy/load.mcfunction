# Wheat War economy — declare the CobbleDollar instability index + scratch/const objectives.
# Registered in #minecraft:load. Idempotent: 'objectives add' no-ops if it exists, and
# cd_instability is only seeded to 0 when UNSET, so it persists across relogs (world data).
scoreboard objectives add cd_instability dummy
scoreboard objectives add cd_calc dummy
scoreboard objectives add cd_const dummy
scoreboard players set #four cd_const 4
scoreboard players set #hundred cd_const 100
execute unless score #idx cd_instability matches -2147483648..2147483647 run scoreboard players set #idx cd_instability 0

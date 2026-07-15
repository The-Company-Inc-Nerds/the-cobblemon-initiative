# LONG-TERM GROWTH VEHICLE — 500 CD Magikarp, BALANCE-GATED. Was a bare `cobbledollars remove`
# that (because remove soft-clamps at 0) handed the fish to a broke player for FREE. Run AS the
# player (dialog button, as_player=true). Pay-probe: a net-zero self-pay, store RESULT before
# removing (0 = broke, 500 = can afford). cd_calc is declared by economy/load (#minecraft:load).
scoreboard players set #karp_ok cd_calc 0
execute store result score #karp_ok cd_calc run cobbledollars pay @s 500
execute if score #karp_ok cd_calc matches 1.. run cobbledollars remove @s 500
execute if score #karp_ok cd_calc matches 1.. run cobblemon-initiative givemon magikarp level=5
execute if score #karp_ok cd_calc matches 1.. run give @s cobblemon:oran_berry 1
execute if score #karp_ok cd_calc matches 1.. run tag @s add bought_magikarp
execute if score #karp_ok cd_calc matches 1.. run title @s actionbar {"text":"Asset transferred. One long-term growth vehicle, one complimentary onboarding berry.","color":"gold"}
execute if score #karp_ok cd_calc matches 0 run title @s actionbar {"text":"The vehicle does not leave the lot on credit. Come back with five hundred that clears.","color":"gray"}

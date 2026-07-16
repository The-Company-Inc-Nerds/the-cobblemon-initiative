# Buy a Frontier hall pass — 200 CD, a pure prestige/CD sink (soft: never consumed, never
# gates a battle). Pay-probe idiom (verified route/decline_sq_genji_wager pattern): `pay`
# soft-fails when broke, so `store result` = 0 (broke) or the amount (affordable); `remove`
# does the single actual deduction. CobbleDollars clamps at 0, so this can never go negative.
scoreboard players set #pass_ok cd_calc 0
execute store result score #pass_ok cd_calc run cobbledollars pay @s 200
execute if score #pass_ok cd_calc matches 1.. run cobbledollars remove @s 200
execute if score #pass_ok cd_calc matches 1.. run scoreboard players add @s frontier_passes 1
execute if score #pass_ok cd_calc matches 1.. run title @s actionbar [{"text":"Verified Charge: 200 CD. ","color":"gold"},{"text":"One hall pass issued.","color":"gray"}]
execute if score #pass_ok cd_calc matches 0 run title @s actionbar [{"text":"The window stays shut to the short of funds. Come back with 200.","color":"gray"}]

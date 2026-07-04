# Paid nurse healing. Run as the player (a healer NPC's heal button). Charges a flat fee,
# then fully heals the party — ONLY if the player can afford it.
# Balance gate (bytecode-verified, CobbleDollars 2.0.0-Beta-5.1): `pay` checks the SOURCE
# balance before any mutation and a self-pay is net-zero (deduct then re-read then add on
# the same live field). The fail path soft-fails (returns 0 WITHOUT throwing), so `store
# success` would read 1 either way — `store result` is the reliable signal: 0 = broke,
# amount (100) = paid. Reset first so a parse-denied line can't leave a stale score.
scoreboard players set #heal_ok cd_calc 0
execute store result score #heal_ok cd_calc run cobbledollars pay @s 100
execute if score #heal_ok cd_calc matches 1.. run cobbledollars remove @s 100
execute if score #heal_ok cd_calc matches 1.. run healpokemon @s
execute if score #heal_ok cd_calc matches 1.. run title @s actionbar [{"text":"Your team is fully healed. ","color":"green"},{"text":"Service fee: 100 CobbleDollars.","color":"gray"}]
execute if score #heal_ok cd_calc matches 0 run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"The Company does not extend credit. (100 CD required)","color":"gray"}]

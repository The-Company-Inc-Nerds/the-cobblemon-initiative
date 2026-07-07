# Paid nurse healing, macro half. Args: fee (computed by economy/heal_paid — never call
# this directly; the fee derivation and this probe must stay one transaction).
# Balance gate (bytecode-verified, CobbleDollars 2.0.0-Beta-5.1): `pay` checks the SOURCE
# balance before any mutation and a self-pay is net-zero (deduct then re-read then add on
# the same live field). The fail path soft-fails (returns 0 WITHOUT throwing), so `store
# success` would read 1 either way — `store result` is the reliable signal: 0 = broke,
# amount = paid. Reset first so a parse-denied line can't leave a stale score.
scoreboard players set #heal_ok cd_calc 0
$execute store result score #heal_ok cd_calc run cobbledollars pay @s $(fee)
$execute if score #heal_ok cd_calc matches 1.. run cobbledollars remove @s $(fee)
execute if score #heal_ok cd_calc matches 1.. run healpokemon @s
$execute if score #heal_ok cd_calc matches 1.. run title @s actionbar [{"text":"Your team is fully healed. ","color":"green"},{"text":"Service fee at the posted rate: $(fee) CobbleDollars.","color":"gray"}]
$execute if score #heal_ok cd_calc matches 0 run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"The Center does not extend credit. ($(fee) CD required)","color":"gray"}]

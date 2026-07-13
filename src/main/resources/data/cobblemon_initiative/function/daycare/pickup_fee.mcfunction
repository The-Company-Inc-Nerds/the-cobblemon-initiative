# Daycare pickup fee, macro half. Args: fee (computed by DaycareManager.withdraw — never
# call this directly; the fee derivation, this probe, and the party return must stay one
# transaction). Run AS the withdrawing player (Java dispatches with the player's source).
# Balance gate (bytecode-verified, CobbleDollars 2.0.0-Beta-5.1): `pay` checks the SOURCE
# balance before any mutation and a self-pay is net-zero (deduct then re-read then add on
# the same live field). The fail path soft-fails (returns 0 WITHOUT throwing), so `store
# success` would read 1 either way — `store result` is the reliable signal: 0 = broke,
# amount = paid. Reset first so a parse-denied line can't leave a stale score. Java reads
# #dc_ok back and branches: paid → mon returns to the party; broke → it boards longer.
# (cd_calc is declared by economy/load in #minecraft:load.)
scoreboard players set #dc_ok cd_calc 0
$execute store result score #dc_ok cd_calc run cobbledollars pay @s $(fee)
$execute if score #dc_ok cd_calc matches 1.. run cobbledollars remove @s $(fee)

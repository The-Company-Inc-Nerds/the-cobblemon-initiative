# Safari Day Permit pay-probe, macro half. Args: fee (SafariManager dispatches this AS
# THE PLAYER with {fee:N} and reads #sf_ok back — never call it with a rolled amount;
# committed fees are flat and printed first, per the randomness invariant).
# Balance gate (bytecode-verified, CobbleDollars 2.0.0-Beta-5.1): `pay` checks the SOURCE
# balance before any mutation and a self-pay is net-zero (deduct then re-read then add on
# the same live field). The fail path soft-fails (returns 0 WITHOUT throwing), so `store
# success` would read 1 either way — `store result` is the reliable signal: 0 = broke,
# amount = paid. Reset first so a parse-denied line can't leave a stale score.
scoreboard players set #sf_ok cd_calc 0
$execute store result score #sf_ok cd_calc run cobbledollars pay @s $(fee)
$execute if score #sf_ok cd_calc matches 1.. run cobbledollars remove @s $(fee)
$execute if score #sf_ok cd_calc matches 0 run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"The Preserve does not extend credit. ($(fee) CD required)","color":"gray"}]

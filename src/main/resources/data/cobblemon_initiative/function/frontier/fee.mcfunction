# Frontier fee pay-probe, macro half — same contract as safari/permit_fee (soft-fail
# `pay`, gate on store result). Args: fee. Dispatched by FrontierManager AS the buying
# player; the manager reads #fr_ok one tick later. 0 = broke/declined, >=1 = paid.
scoreboard players set #fr_ok cd_calc 0
$execute store result score #fr_ok cd_calc run cobbledollars pay @s $(fee)
$execute if score #fr_ok cd_calc matches 1.. run cobbledollars remove @s $(fee)
$execute if score #fr_ok cd_calc matches 0 run title @s actionbar [{"text":"Declined. ","color":"red"},{"text":"The stall does not run tabs. ($(fee) CD required)","color":"gray"}]

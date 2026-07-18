# Bait kiosk pay-probe, macro half — same contract as permit_fee (see that file for the
# store-result rationale: CobbleDollars `pay` soft-fails, so `store success` lies and only
# `store result` distinguishes broke from paid). Args: fee. Dispatched by
# SafariManager.buyBait AS the buying player; the manager reads #sfb_ok one tick later
# and only then issues the bait. #sfb_ok 0 = broke/declined, >=1 = paid.
scoreboard players set #sfb_ok cd_calc 0
$execute store result score #sfb_ok cd_calc run cobbledollars pay @s $(fee)
$execute if score #sfb_ok cd_calc matches 1.. run cobbledollars remove @s $(fee)
$execute if score #sfb_ok cd_calc matches 0 run title @s actionbar [{"text":"Purchase declined. ","color":"red"},{"text":"The kiosk does not extend credit. ($(fee) CD required)","color":"gray"}]

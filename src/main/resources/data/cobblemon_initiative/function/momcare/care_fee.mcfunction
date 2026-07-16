# Mom's care pickup fee — ONLY dispatched when mom_care_fee > 0 (free by default; her care is
# a mother's, not a ledger's). Args: fee (from MomCareManager.withdraw). Run AS the player. Same
# CobbleDollars pay-probe rail as the daycare; MomCareManager reads #mc_ok back next tick and
# returns the boarded mon on success. (cd_calc is declared by economy/load.)
scoreboard players set #mc_ok cd_calc 0
$execute store result score #mc_ok cd_calc run cobbledollars pay @s $(fee)
$execute if score #mc_ok cd_calc matches 1.. run cobbledollars remove @s $(fee)

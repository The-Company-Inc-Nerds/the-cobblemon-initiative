# stake_assessor - Roderick (sq_deepcore_assessor): the 250 CD dojo wager stake,
# broke-probe (copied from route/stake_sq_hz_analyst). Runs AS the player (as_player bare
# function call from every path into his battle - Kang's wager_offer and the Officer's own
# recognition dialog both call this, so all paths pay the same stake). Paid -> stake charged,
# battle fires (win returns 500 = stake doubled; a loss costs exactly the stake - loss_fee
# deliberately unset). BROKE -> NO battle: you cannot stake money you do not have. The 120 CD
# bow-out (route/decline_sq_deepcore_assessor) is the broke player's forced-fight path.
scoreboard players set #stk_ok cd_calc 0
execute unless entity @s[tag=defeated_sq_deepcore_assessor] unless entity @s[tag=declined_sq_deepcore_assessor] store result score #stk_ok cd_calc run cobbledollars pay @s 250
execute if score #stk_ok cd_calc matches 1.. run cobbledollars remove @s 250
execute if score #stk_ok cd_calc matches 1.. run title @s actionbar [{"text":"Stake logged: 250 CD. ","color":"gold"},{"text":"The vault wager is live.","color":"gray"}]
# Win side carries the shipped wager_sweetener rider (+25..100 CD, announced; the 500 return and
# the 250 stake stay FIXED - committed amounts never roll). onwin lists are single-quoted SNBT.
execute if score #stk_ok cd_calc matches 1.. run tbcs battle GEN_9_SINGLES @s vs rctmod:sq_deepcore_assessor onwin {1: ['cobbledollars give @1 500', 'tag @1 add defeated_sq_deepcore_assessor', '@2 say Adjustment reversed. The vault is yours to read. I was told you were a closed file - the file appears to be open.', 'execute as @1 run function cobblemon_initiative:economy/wager_sweetener'], 2: ['@1 say Non-compliance is billable. The stake reconciles in the Company favor. Do enjoy the receipt.']}
execute if score #stk_ok cd_calc matches 0 unless entity @s[tag=defeated_sq_deepcore_assessor] unless entity @s[tag=declined_sq_deepcore_assessor] run title @s actionbar [{"text":"Stake declined. ","color":"red"},{"text":"The vault does not extend credit. (250 CD required)","color":"gray"}]

# stake_sq_hz_analyst — Lan (hz_analyst): the 150 CD Customer Confidence Challenge
# stake, broke-probe retrofit (round 13b). Runs AS the player (as_player bare function
# call from every path into her battle — all of them pay the same stake, established
# rule). Paid -> stake charged, challenge announced, the battle fires (win returns 300
# = stake doubled; a loss costs exactly the stake — loss_fee deliberately unset).
# BROKE -> NO battle: you cannot stake money you do not have (the old bare remove
# clamped at 0 and ran the wager as a free lottery). The 80 CD bow-out (route/
# decline_sq_hz_analyst) is the broke player's forced-fight path.
scoreboard players set #stk_ok cd_calc 0
execute unless entity @s[tag=defeated_sq_hz_analyst] unless entity @s[tag=declined_sq_hz_analyst] store result score #stk_ok cd_calc run cobbledollars pay @s 150
execute if score #stk_ok cd_calc matches 1.. run cobbledollars remove @s 150
execute if score #stk_ok cd_calc matches 1.. run title @s actionbar [{"text":"Stake logged: 150 CD. ","color":"gold"},{"text":"The Customer Confidence Challenge is live.","color":"gray"}]
# Win side carries the review-B6 purse sweetener (rolled +25..100 CD rider, announced;
# the 300 return and the 150 stake stay FIXED — committed amounts never roll).
execute if score #stk_ok cd_calc matches 1.. run tbcs battle GEN_9_SINGLES @s vs rctmod:sq_hz_analyst onwin {1: ['cobbledollars give @1 300', 'tag @1 add defeated_sq_hz_analyst', '@2 say Refund issued, doubled, logged as a confidence event. The model did not see you coming. Neither did the model maker.', 'execute as @1 run function cobblemon_initiative:sidequest/minutes/billable_hours', 'execute as @1 run function cobblemon_initiative:economy/wager_sweetener'], 2: ['@1 say The stake stays with the house. Customer confidence: measurably improved. Thank you for participating in the figures.']}
execute if score #stk_ok cd_calc matches 0 unless entity @s[tag=defeated_sq_hz_analyst] unless entity @s[tag=declined_sq_hz_analyst] run title @s actionbar [{"text":"Stake declined. ","color":"red"},{"text":"The branch does not extend credit. (150 CD required)","color":"gray"}]

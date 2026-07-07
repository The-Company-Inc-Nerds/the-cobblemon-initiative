# stake_sq_headcount_wager — Field Researcher Ume: the 300 CD field-wager stake against
# the 900 CD purse, broke-probe retrofit (round 13b). Runs AS the player. Paid -> stake
# charged + the battle fires. BROKE -> NO battle: the stake must be covered in full
# (the old bare remove clamped at 0 and ran the wager stakeless). The 150 CD bow-out
# (route/decline_sq_headcount_wager) is the broke player's forced-fight path.
scoreboard players set #stk_ok cd_calc 0
execute unless entity @s[tag=defeated_sq_headcount_wager] unless entity @s[tag=declined_sq_headcount_wager] store result score #stk_ok cd_calc run cobbledollars pay @s 300
execute if score #stk_ok cd_calc matches 1.. run cobbledollars remove @s 300
execute if score #stk_ok cd_calc matches 1.. run title @s actionbar [{"text":"Stake accepted: 300 CD ","color":"gold"},{"text":"against a 900 CD purse.","color":"gray"}]
# Win side carries the review-B6 purse sweetener (rolled +25..100 CD rider, announced;
# the 900 base purse and the 300 stake stay FIXED — committed amounts never roll).
execute if score #stk_ok cd_calc matches 1.. run tbcs battle GEN_9_SINGLES @s vs rctmod:sq_headcount_wager onwin {1: ['cobbledollars give @1 900', 'tag @1 add defeated_sq_headcount_wager', 'execute as @1 run function cobblemon_initiative:economy/wager_sweetener', '@2 say Documented. The purse is yours and the appendix is thicker for it.'], 2: ['@1 say The stake goes to science. Payroll will find that hilarious.']}
execute if score #stk_ok cd_calc matches 0 unless entity @s[tag=defeated_sq_headcount_wager] unless entity @s[tag=declined_sq_headcount_wager] run title @s actionbar [{"text":"Stake declined. ","color":"red"},{"text":"The appendix requires collateral in full. (300 CD required)","color":"gray"}]

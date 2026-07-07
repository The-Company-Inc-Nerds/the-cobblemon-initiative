# checkpoint_fee_1 — Sani (villain_grunt_field_agent, trainer villain_grunt_1): the
# 120 CD Voluntary Verification processing fee, retrofitted with the broke-probe
# (round 13b). Runs AS the player (as_player bare function call from the fee button).
# CobbleDollars `pay` soft-fails when broke — gate on `store result` (0 = broke,
# 120 = paid; ENGINE_FINDINGS §2). Paid -> fee charged + paid_checkpoint_fee (shared
# with Haruki — paying once covers the checkpoint). Broke -> fee-or-fight: the SAME
# battle line the refuse button carries (a broke click can no longer be verified in
# absentia for free — the old bare `cobbledollars remove` clamped at 0 and tagged anyway).
scoreboard players set #fee_ok cd_calc 0
execute unless entity @s[tag=paid_checkpoint_fee] store result score #fee_ok cd_calc run cobbledollars pay @s 120
execute if score #fee_ok cd_calc matches 1.. run cobbledollars remove @s 120
execute if score #fee_ok cd_calc matches 1.. run tag @s add paid_checkpoint_fee
execute if score #fee_ok cd_calc matches 1.. run title @s actionbar [{"text":"Expedited processing fee received. ","color":"gold"},{"text":"You have been verified in absentia.","color":"gray"}]
execute if score #fee_ok cd_calc matches 0 unless entity @s[tag=paid_checkpoint_fee] run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"Verification proceeds by other means. (120 CD required)","color":"gray"}]
execute if score #fee_ok cd_calc matches 0 unless entity @s[tag=paid_checkpoint_fee] unless entity @s[tag=defeated_villain_grunt_1] run tbcs battle GEN_9_SINGLES @s vs rctmod:villain_grunt_1 onwin {1: ['cobbledollars give @1 230', 'tag @1 add defeated_villain_grunt_1', '@2 say This goes in my performance review, does it not.', 'easy_npc delete @2'], 2: ['cobbledollars remove @2 100', '@1 say Asset recovered. The Company thanks you.']}

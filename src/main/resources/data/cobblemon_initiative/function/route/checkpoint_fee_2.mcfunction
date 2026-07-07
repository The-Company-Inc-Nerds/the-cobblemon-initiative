# checkpoint_fee_2 — Haruki (villain_grunt_2): the 120 CD Voluntary Verification
# processing fee, broke-probe retrofit (round 13b). Same semantics as checkpoint_fee_1
# (see there); the broke path battles THIS agent. paid_checkpoint_fee is shared —
# paying either agent covers the whole checkpoint.
scoreboard players set #fee_ok cd_calc 0
execute unless entity @s[tag=paid_checkpoint_fee] store result score #fee_ok cd_calc run cobbledollars pay @s 120
execute if score #fee_ok cd_calc matches 1.. run cobbledollars remove @s 120
execute if score #fee_ok cd_calc matches 1.. run tag @s add paid_checkpoint_fee
execute if score #fee_ok cd_calc matches 1.. run title @s actionbar [{"text":"Expedited processing fee received. ","color":"gold"},{"text":"You have been verified in absentia.","color":"gray"}]
execute if score #fee_ok cd_calc matches 0 unless entity @s[tag=paid_checkpoint_fee] run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"Verification proceeds by other means. (120 CD required)","color":"gray"}]
execute if score #fee_ok cd_calc matches 0 unless entity @s[tag=paid_checkpoint_fee] unless entity @s[tag=defeated_villain_grunt_2] run tbcs battle GEN_9_SINGLES @s vs rctmod:villain_grunt_2 onwin {1: ['cobbledollars give @1 240', 'tag @1 add defeated_villain_grunt_2', '@2 say Logged as an unscheduled deviation. The outpost will note your face.', 'easy_npc delete @2'], 2: ['cobbledollars remove @2 110', '@1 say Asset secured. The Company appreciates your cooperation.']}

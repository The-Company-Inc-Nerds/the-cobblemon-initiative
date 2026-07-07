# checkpoint_handling_2 — Haruki (villain_grunt_2): the 250 CD unverified-mail handling
# fee, broke-probe retrofit (round 13b). Same semantics as checkpoint_handling_1; the
# broke path battles THIS agent. paid_handling_fee is shared across the checkpoint.
scoreboard players set #fee_ok cd_calc 0
execute unless entity @s[tag=paid_handling_fee] store result score #fee_ok cd_calc run cobbledollars pay @s 250
execute if score #fee_ok cd_calc matches 1.. run cobbledollars remove @s 250
execute if score #fee_ok cd_calc matches 1.. run tag @s add paid_handling_fee
execute if score #fee_ok cd_calc matches 1.. run title @s actionbar [{"text":"Handling fee logged. ","color":"gold"},{"text":"The letter remains your liability. The Company thanks you for participating in commerce.","color":"gray"}]
execute if score #fee_ok cd_calc matches 0 unless entity @s[tag=paid_handling_fee] run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"Category two irregularities are processed differently. (250 CD required)","color":"gray"}]
execute if score #fee_ok cd_calc matches 0 unless entity @s[tag=paid_handling_fee] unless entity @s[tag=defeated_villain_grunt_2] run tbcs battle GEN_9_SINGLES @s vs rctmod:villain_grunt_2 onwin {1: ['cobbledollars give @1 240', 'tag @1 add defeated_villain_grunt_2', '@2 say Logged as an unscheduled deviation. The outpost will note your face.', 'easy_npc delete @2'], 2: ['cobbledollars remove @2 110', '@1 say Asset secured. The Company appreciates your cooperation.']}

# checkpoint_handling_1 — Sani (villain_grunt_field_agent): the 250 CD unverified-mail
# handling fee (contraband entry), broke-probe retrofit (round 13b). Paid -> fee +
# paid_handling_fee (shared with Haruki; the letter stays with the player). Broke ->
# fee-or-fight: the surrender fork remains the free-of-charge compliance option.
scoreboard players set #fee_ok cd_calc 0
execute unless entity @s[tag=paid_handling_fee] store result score #fee_ok cd_calc run cobbledollars pay @s 250
execute if score #fee_ok cd_calc matches 1.. run cobbledollars remove @s 250
execute if score #fee_ok cd_calc matches 1.. run tag @s add paid_handling_fee
execute if score #fee_ok cd_calc matches 1.. run tag @s add checkpoint_settled
execute if score #fee_ok cd_calc matches 1.. run title @s actionbar [{"text":"Handling fee logged. ","color":"gold"},{"text":"The letter remains your liability. The Company thanks you for participating in commerce.","color":"gray"}]
execute if score #fee_ok cd_calc matches 0 unless entity @s[tag=paid_handling_fee] run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"Category two irregularities are processed differently. (250 CD required)","color":"gray"}]
execute if score #fee_ok cd_calc matches 0 unless entity @s[tag=paid_handling_fee] unless entity @s[tag=defeated_villain_grunt_1] run tbcs battle GEN_9_SINGLES @s vs rctmod:villain_grunt_1 onwin {1: ['cobbledollars give @1 230', 'tag @1 add defeated_villain_grunt_1', '@2 say This goes in my performance review, does it not.', 'easy_npc delete @2'], 2: ['cobbledollars remove @2 100', '@1 say Asset recovered. The Company thanks you.']}

# Off the Record errand 2 turn-in at Sarii. Run as the player. Pays in kind (bread + oran)
# and unlocks errand 3 (the inversion) by arming off_errand_3_active - the auditor dialog
# stand_and_be_counted entry gates on it.
tag @s add errand2_done
tag @s remove carrying_basket
give @s minecraft:bread 3
give @s cobblemon:oran_berry 4
tag @s add off_errand_3_active
execute if score @s obs_count matches ..2 run title @s actionbar [{"text":"Basket delivered - unseen. ","color":"gold"},{"text":"Last errand: let the auditors log you on purpose.","color":"gray"}]
execute if score @s obs_count matches 3.. run title @s actionbar [{"text":"Basket delivered. ","color":"gold"},{"text":"They saw you again. Last errand: walk right up and be logged.","color":"gray"}]

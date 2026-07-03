# Off the Record errand 1 turn-in at Oma. Run as the player, BEFORE the dialog resets
# obs_count for errand 2. Full pay (250 CD) if this carry drew two logs or fewer; half
# (125) if the clipboard caught you three or more times. Fail-soft: never blocks, never hurts.
tag @s add errand1_done
tag @s remove carrying_ledger
execute if score @s obs_count matches ..2 run function cobblemon_initiative:economy/payout {amount:250}
execute if score @s obs_count matches ..2 run title @s actionbar [{"text":"Satchel delivered - off the record.","color":"gold"}]
execute if score @s obs_count matches 3.. run function cobblemon_initiative:economy/payout {amount:125}
execute if score @s obs_count matches 3.. run title @s actionbar [{"text":"Delivered - but the clipboard caught you. Half the quiet money.","color":"yellow"}]

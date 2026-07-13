# Assistant Miri hand-in: 16 kelp. Run as the player.
execute store result score #turnin ci_item run clear @s minecraft:kelp 0
execute if score #turnin ci_item matches ..15 run title @s actionbar [{"text":"You need 16 kelp fronds to hand in.","color":"red"}]
execute if score #turnin ci_item matches 16.. run clear @s minecraft:kelp 16
execute if score #turnin ci_item matches 16.. run function cobblemon_initiative:economy/reward/minor
execute if score #turnin ci_item matches 16.. run function cobblemon_initiative:economy/payout {amount:250}
execute if score #turnin ci_item matches 16.. run tag @s add quest_assist_collect_done
execute if score #turnin ci_item matches 16.. run title @s actionbar [{"text":"Collections logged. Grant work: another ledger closed.","color":"yellow"}]

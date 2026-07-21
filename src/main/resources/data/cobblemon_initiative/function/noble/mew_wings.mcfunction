# A Giggle in the Grass. Run as @s = the player (the chase giver's chase_button runs
# `function cobblemon_initiative:noble/mew_wings` as_player, before `noble start mew`).
#
# Mew will not stand and fight — it blinks and runs. Lend the chaser a borrowed pair
# of wings so they can keep pace across the tall grass and corner the wisp. This is an
# aid for the chase, NOT a reward. Guarded so a relog/re-chase cannot re-grant it.
execute unless entity @s[tag=mew_wings_given] run give @s minecraft:elytra 1
execute unless entity @s[tag=mew_wings_given] run give @s minecraft:firework_rocket 16
execute unless entity @s[tag=mew_wings_given] run tellraw @s {"text":"Borrowed wings for the chase — the wisp runs, so run with it. Give them back when the grass goes quiet.","color":"aqua"}
tag @s add mew_wings_given

# Sango Classic — shared win teardown: stop the clock, drop the bar, roll the fanfare.
scoreboard players set #on ci_classic 0
tag @s remove classic_active
bossbar set cobblemon_initiative:sango_classic visible false
title @s title [{"text":"RECORD QUARTER","color":"aqua","bold":true}]
title @s subtitle [{"text":"Three fish, filed on time","color":"gray"}]
playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 1
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 0.8 1.4
# Record Species bonus (review B6): the entry-roll species landed in THIS quarter's
# delta (the ci_fish_* deltas are still live from turnin; take_fish never mutates them)
# -> +75 CD through the skew-aware payout. Money-only, so the repeatable branch stays
# farm-safe; an unset #species (quarter begun pre-update) simply never matches.
scoreboard players set @s ci_classic_bonus 0
execute if score #species ci_classic matches 1 if score @s ci_fish_cod matches 1.. run scoreboard players set @s ci_classic_bonus 1
execute if score #species ci_classic matches 2 if score @s ci_fish_salmon matches 1.. run scoreboard players set @s ci_classic_bonus 1
execute if score #species ci_classic matches 3 if score @s ci_fish_puffer matches 1.. run scoreboard players set @s ci_classic_bonus 1
execute if score #species ci_classic matches 4 if score @s ci_fish_tropical matches 1.. run scoreboard players set @s ci_classic_bonus 1
execute if score @s ci_classic_bonus matches 1 run function cobblemon_initiative:economy/payout {amount:75}
execute if score @s ci_classic_bonus matches 1 run tellraw @s [{"text":"RECORD SPECIES LANDED — ","color":"aqua","bold":true},{"text":"the chalkboard pays +75 CD on the purse.","color":"gray"}]

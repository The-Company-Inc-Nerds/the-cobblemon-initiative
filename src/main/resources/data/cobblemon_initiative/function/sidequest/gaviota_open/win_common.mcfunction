# Gaviota Open (alpha.26 audit rulings) — shared win teardown: stop the clock, drop the
# bar, roll the fanfare. Sango derby/win_common clone.
scoreboard players set #on ci_open 0
tag @s remove gaviota_open_active
bossbar set cobblemon_initiative:gaviota_open visible false
title @s title [{"text":"RECORD ROUND","color":"aqua","bold":true}]
title @s subtitle [{"text":"Ten fish, verified at the scale","color":"gray"}]
playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 1
playsound minecraft:block.note_block.bell master @s ~ ~ ~ 0.8 1.4
# Record Catch bonus (Sango chalkboard idiom): the entry-roll species landed in THIS
# round's delta (the ci_open_* deltas are still live from turnin; take_fish never
# mutates them) -> +100 CD through the skew-aware payout. Money-only, so the repeatable
# branch stays farm-safe; an unset #species (round begun pre-update) never matches.
scoreboard players set @s ci_open_bonus 0
execute if score #species ci_open matches 1 if score @s ci_open_cod matches 1.. run scoreboard players set @s ci_open_bonus 1
execute if score #species ci_open matches 2 if score @s ci_open_salmon matches 1.. run scoreboard players set @s ci_open_bonus 1
execute if score #species ci_open matches 3 if score @s ci_open_puffer matches 1.. run scoreboard players set @s ci_open_bonus 1
execute if score #species ci_open matches 4 if score @s ci_open_ink matches 1.. run scoreboard players set @s ci_open_bonus 1
execute if score @s ci_open_bonus matches 1 run function cobblemon_initiative:economy/payout {amount:100}
execute if score @s ci_open_bonus matches 1 run tellraw @s [{"text":"RECORD CATCH LANDED — ","color":"aqua","bold":true},{"text":"the chalkboard pays +100 CD on the purse.","color":"gray"}]

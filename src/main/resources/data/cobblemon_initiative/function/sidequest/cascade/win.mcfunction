# Cascade Ascent — crest reached in time. Run as/at the winning player.
# ORDER MATTERS: check the gold path (tag already present) BEFORE win_first adds the tag,
# so a first clear never double-pays.
tag @s remove ci_ascending
bossbar set cobblemon_initiative:cascade visible false
execute at @s run playsound minecraft:ui.toast.challenge_complete player @s ~ ~ ~ 1 1
execute if entity @s[tag=sq_cascade_done] run function cobblemon_initiative:sidequest/cascade/win_gold
execute unless entity @s[tag=sq_cascade_done] run function cobblemon_initiative:sidequest/cascade/win_first

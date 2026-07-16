# SIGNAL INTEGRITY - board 2 of 3. Run as the player from the prop scrub button.
# Idempotent: the actionbar + sound only fire on the first scrub; the tag add re-asserts harmlessly.
execute unless entity @s[tag=ci_board_2] run title @s actionbar [{"text":"Feed scrubbed ","color":"gold"},{"text":"(2/3)","color":"yellow"},{"text":": another board stops broadcasting the cover-up.","color":"gray"}]
execute unless entity @s[tag=ci_board_2] run playsound minecraft:block.beacon.deactivate master @s ~ ~ ~ 0.8 1.0
tag @s add ci_board_2
function cobblemon_initiative:quest/refresh

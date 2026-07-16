# THE DOOR DOWNTOWN - deliver the HQ-gate pointer. Run as the player. Called on the wager WIN,
# the wager LOSS (both wired on cyber_grid_broker.battle on_win/on_lose), and the paid decline
# (door/decline_wager). ONE-SHOT: guarded on hq_pointer_done so it never double-fires. Speaks the
# raid-gate math (badges 7, fields 4 - owned by 10_hq_raid.md), points at Fenceline/Coldfurrow, and
# names the tower lobby [1590 51 1028]. Clean text (no double-quotes / apostrophes) so the two beats
# read as one continuous instruction with the keycard marquee.
execute unless entity @s[tag=hq_pointer_done] run title @s title [{"text":"THE DOOR DOWNTOWN","color":"gold","bold":true}]
execute unless entity @s[tag=hq_pointer_done] run title @s subtitle [{"text":"badges seven, fields four, then the tower.","color":"gray"}]
execute unless entity @s[tag=hq_pointer_done] run tellraw @s [{"text":"THE DOOR DOWNTOWN - ","color":"gold","bold":true},{"text":"the tower door opens on seven badges and four liberated fields. Starve the last feeders first: Fenceline Acres south, Coldfurrow east on the Ryujin road. Then walk into the lobby at ","color":"gray"},{"text":"[1590 51 1028]","color":"red"},{"text":".","color":"gray"}]
execute unless entity @s[tag=hq_pointer_done] run playsound minecraft:block.beacon.activate master @s ~ ~ ~ 0.8 1.0
tag @s add hq_pointer_done
function cobblemon_initiative:quest/refresh

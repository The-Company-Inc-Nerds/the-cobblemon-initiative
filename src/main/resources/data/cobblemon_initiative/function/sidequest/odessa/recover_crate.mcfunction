# The Tide Market - the turn-in. Run as the player, fired by the customs-float crate prop
# (a right-click / interaction command block at the seized-crate coord ~[540 63 3530], off the
# far slip - no character file). One-shot: guarded on started-and-not-yet-recovered so it only
# pays and unlocks once. Sets odessa_crate_recovered (unlocks Odessa open_shop -> trade_black_market),
# clears the started latch, pays a 150 CD finder token via the skewed payout, announces the sting.
execute if entity @s[tag=odessa_crate_started,tag=!odessa_crate_recovered] run function cobblemon_initiative:economy/payout {amount:150}
execute if entity @s[tag=odessa_crate_started,tag=!odessa_crate_recovered] run tag @s add odessa_crate_recovered
tag @s remove odessa_crate_started
execute if entity @s[tag=odessa_crate_recovered] run title @s title [{"text":"CRATE RECOVERED","color":"gold","bold":true}]
execute if entity @s[tag=odessa_crate_recovered] run title @s subtitle [{"text":"The tide market opens.","color":"gray"}]
execute if entity @s[tag=odessa_crate_recovered] run playsound minecraft:entity.player.levelup master @s ~ ~ ~ 0.6 1.2
function cobblemon_initiative:quest/refresh

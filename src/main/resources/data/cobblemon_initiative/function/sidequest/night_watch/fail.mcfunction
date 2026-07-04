# First Night Watch — the watcher left the fence past the grace window. FAIL-SOFT:
# no damage, no cost, no latch consumed — the lantern takes a flame again next dusk.
# Run as/at the player.
tag @s remove ci_watching
scoreboard players set @s nw_grace 0
bossbar set cobblemon_initiative:night_watch visible false
title @s title [{"text":"THE WATCH BROKE","color":"red"}]
title @s subtitle [{"text":"No harm done — the lantern takes a flame again at dusk","color":"gray"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.6

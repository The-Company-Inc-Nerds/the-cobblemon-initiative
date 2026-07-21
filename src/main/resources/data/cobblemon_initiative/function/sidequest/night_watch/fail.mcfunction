# First Night Watch — the watcher left the fence past the grace window. FAIL-SOFT:
# no damage, no cost, no latch consumed — the lantern takes a flame again next dusk.
# Run as/at the player.
tag @s remove ci_watching
scoreboard players set @s nw_grace 0
# Clear any scripted watch-mobs so a broken watch doesn't leave a swarm on the field.
kill @e[tag=nw_mob,distance=..128]
bossbar set cobblemon_initiative:night_watch visible false
title @s title [{"text":"THE WATCH BROKE","color":"red"}]
title @s subtitle [{"text":"No harm done — the lantern takes a flame again at dusk","color":"gray"}]
playsound minecraft:block.note_block.bass master @s ~ ~ ~ 1 0.6

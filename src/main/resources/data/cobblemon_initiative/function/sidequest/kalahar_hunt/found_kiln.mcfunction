# MACRO — the kiln cache was opened. Retire the KILN hunt (grab-and-keep = done on open) and make
# it repeatable: clear the active flag + everyone's kiln_hunt_active tag, restore the dug column to
# plain sand (chunk is loaded — the player is standing here), and confirm. Nadia's "start a hunt"
# button is gated on the flag being idle, so re-arming now buries a fresh cache. NOTE macro args
# come from the storage kiln compound (this fn is reached from check_kiln_apply, itself a macro).
$setblock $(cbx) $(cby) $(cbz) minecraft:sand
scoreboard players set #hunt_kiln ci_hunt 0
tag @a remove kiln_hunt_active
title @a[distance=..24] actionbar [{"text":"The kiln-master's cache is yours. The clay-run keeps nothing now.","color":"gold"}]
playsound minecraft:block.decorated_pot.insert master @a[distance=..24] ~ ~ ~ 0.8 1.0

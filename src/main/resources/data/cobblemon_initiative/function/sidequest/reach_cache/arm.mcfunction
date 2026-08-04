# MACRO — arm the active site: forceload its chunk, schedule the placement past the load, and
# chalk the record's bearing for the player. schedule cannot carry macro args, so `place` is a
# plain re-dispatch that re-reads storage `active`.
$forceload add $(cx) $(cz)
schedule function cobblemon_initiative:sidequest/reach_cache/place 2s
$tellraw @s [{"text":"Ossa chalks the record bearing on your map-hand: ","color":"gold"},{"text":"$(dirtext)","color":"yellow"},{"text":". Watch for the dust-shimmer standing over the sand.","color":"gold"}]

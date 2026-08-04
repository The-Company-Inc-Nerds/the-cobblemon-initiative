# MACRO — bury the cache: three suspicious-sand blocks AT the scanned surface columns (they read
# as plain sand until brushed). Center block carries the DEED token table; flankers carry minor
# archaeology loot. Blocks do NOT regenerate — clean/turnin restores plain sand.
$setblock $(x1) $(y1) $(z1) minecraft:suspicious_sand{LootTable:"cobblemon_initiative:sidequest/reach_cache_token"}
$setblock $(x2) $(y2) $(z2) minecraft:suspicious_sand{LootTable:"cobblemon_initiative:sidequest/reach_cache_minor"}
$setblock $(x3) $(y3) $(z3) minecraft:suspicious_sand{LootTable:"cobblemon_initiative:sidequest/reach_cache_minor"}
$forceload remove $(cx) $(cz)

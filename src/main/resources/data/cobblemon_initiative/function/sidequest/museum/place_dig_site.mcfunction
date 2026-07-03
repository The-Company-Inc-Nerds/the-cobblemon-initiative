# Museum dig site — SHOWRUNNER ONE-COMMAND PASTE. Stand at the center of the chosen
# strata spot below the falls (SOLID ground — suspicious gravel falls if unsupported) and run:
#   /function cobblemon_initiative:sidequest/museum/place_dig_site
# Rings 10 suspicious_gravel at feet level (~ ~-1 ~ pattern), each wired to the
# cobblemon_initiative:archaeology/takehara_dig loot table. Blocks do NOT regenerate
# (fine for a one-shot run); re-run at a new spot for a fresh site.
setblock ~2 ~-1 ~ minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~3 ~-1 ~1 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~1 ~-1 ~2 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~-1 ~-1 ~3 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~-2 ~-1 ~1 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~-3 ~-1 ~ minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~-2 ~-1 ~-2 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~ ~-1 ~-3 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~2 ~-1 ~-2 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
setblock ~3 ~-1 ~-1 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"} replace
tellraw @s [{"text":"Dig site placed: 10 suspicious gravel in a ring around you, loot table archaeology/takehara_dig.","color":"green"}]

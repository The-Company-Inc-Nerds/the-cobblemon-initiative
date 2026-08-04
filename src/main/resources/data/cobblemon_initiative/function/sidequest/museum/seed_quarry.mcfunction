# Suspicious-gravel seeding for the Takehara gravel quarry (playtest 2026-08-03 N2 + P2).
# Fired ONCE per world from ambient/tick when a brush-carrying player first reaches the pit
# (proximity gate = chunks are loaded, so every setblock lands). Positions were picked from a
# region-scan of EXPOSED gravel (dev/wave_a18_scan/gravel_exposed.json, 494 candidates ->
# every 12th = 42 seeds spread across the y69-82 benches). Brushing uses the archaeology
# loot table cobblemon_initiative:archaeology/takehara_dig (bones/sherds + all 11 fossils).
# Blocks do NOT regenerate - same contract as the museum strata ring.
scoreboard players set #quarry_seeded ci_ambient 1
setblock 2033 69 2619 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2036 69 2619 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2036 70 2622 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2037 71 2622 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2033 72 2621 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2040 72 2620 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2030 73 2601 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2032 73 2602 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2034 73 2596 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2037 73 2594 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2047 73 2618 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2054 73 2600 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2057 73 2596 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2059 73 2598 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2060 73 2606 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2028 74 2603 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2032 74 2622 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2054 74 2594 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2030 75 2619 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2050 75 2593 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2029 76 2619 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2048 76 2593 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2028 77 2601 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2034 77 2594 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2065 77 2590 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2033 78 2595 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2063 78 2590 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2016 79 2612 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2019 79 2608 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2021 79 2608 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2023 79 2626 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2034 79 2635 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2048 79 2590 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2012 80 2614 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2020 80 2607 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2031 80 2596 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2037 80 2590 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2026 81 2602 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2033 81 2639 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2015 82 2609 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2026 82 2601 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
setblock 2034 82 2643 minecraft:suspicious_gravel{LootTable:"cobblemon_initiative:archaeology/takehara_dig"}
execute positioned 2035 72 2620 run title @a[distance=..64,tag=sq_museum_brush] actionbar [{"text":"Some of the quarry gravel looks soft and pale - brush it slowly.","color":"gold"}]

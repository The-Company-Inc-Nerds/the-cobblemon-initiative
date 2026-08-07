# MACRO — bury the cache: a LootTable chest at the dig column, then TWO sand blocks above it so
# the player must dig through the drift to reach the lid (per the giver's clue). Shared by both
# hunts (kiln + warden) — only the loot table differs (kiln = grab-and-keep relics; warden = the
# one marked Warden's Cache chest item). The chest auto-fills on first open; the tick loop then
# sees the LootTable tag consumed and retires the hunt (KILN) — the WARDEN hunt retires on Ossa's
# turn-in. Chunk was force-loaded by arm_apply; drop the forceload once placed.
$setblock $(cbx) $(cby) $(cbz) minecraft:chest[facing=north]{LootTable:"$(loot)"}
$setblock $(cbx) $(s1y) $(cbz) minecraft:sand
$setblock $(cbx) $(s2y) $(cbz) minecraft:sand
$forceload remove $(cx) $(cz)

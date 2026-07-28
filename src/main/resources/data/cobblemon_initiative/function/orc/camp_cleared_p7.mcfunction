# Orc camp P7 cleared — one-shot wrapper, run positioned at the pin from orc/tick when
# the absence poll finds no tagged camp mob within 32. Latch 1 -> 2 FIRST (never re-arms,
# never double-pays), then the shared ceremony, then minor spoils to the NEAREST player.
# Loot namespace is the UNDERSCORE datapack one (cobblemon_initiative:) — dashed is
# item-registry only.
scoreboard players set #orc_camp_p7 ci_ambient 2
function cobblemon_initiative:orc/cleared_ceremony
loot give @a[distance=..64,limit=1,sort=nearest] loot cobblemon_initiative:npc_gift/orc_spoils_minor

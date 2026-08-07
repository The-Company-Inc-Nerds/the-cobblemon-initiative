# MACRO — kiln hunt tick step (site chunk loaded only when the player is near, so all block tests
# self-gate on proximity). Two cases:
#   (a) chest present + still carries LootTable  -> UNOPENED: raise the smoke column.
#   (b) chest present + LootTable consumed        -> OPENED : the player grabbed the loot; retire
#       the hunt (clear flag + everyone's active tag), restore the column to plain sand so a
#       re-arm from Nadia buries a fresh cache, and confirm to nearby players. REPEATABLE.
$execute if block $(cbx) $(cby) $(cbz) minecraft:chest if data block $(cbx) $(cby) $(cbz) LootTable run particle minecraft:campfire_signal_smoke $(smx) $(smy) $(smz) 0.2 1.5 0.2 0.006 4 force
$execute if block $(cbx) $(cby) $(cbz) minecraft:chest if data block $(cbx) $(cby) $(cbz) LootTable run particle minecraft:campfire_cosy_smoke $(smx) $(smy) $(smz) 0.25 2.5 0.25 0.004 8 force
$execute if block $(cbx) $(cby) $(cbz) minecraft:chest unless data block $(cbx) $(cby) $(cbz) LootTable run function cobblemon_initiative:sidequest/kalahar_hunt/found_kiln

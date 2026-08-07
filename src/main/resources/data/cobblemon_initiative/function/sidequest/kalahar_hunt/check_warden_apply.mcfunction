# MACRO — warden hunt tick step (site chunk loaded only when the player is near). While the buried
# chest is UNOPENED, raise the smoke column. When it is OPENED the player has the Warden's Cache
# item in hand — nothing left to follow, so stop smoking and mark the hunt "awaiting turn-in"
# (#hunt_warden = 2). The hunt is NOT retired here: it completes at Ossa's dialog turn-in
# (deposit_warden), which consumes the carried cache, pays, and resets #hunt_warden to 0. Because
# this fn only runs while #hunt_warden matches 1 (see tick.mcfunction), the transition fires once.
$execute if block $(cbx) $(cby) $(cbz) minecraft:chest if data block $(cbx) $(cby) $(cbz) LootTable run particle minecraft:campfire_signal_smoke $(smx) $(smy) $(smz) 0.2 1.5 0.2 0.006 4 force
$execute if block $(cbx) $(cby) $(cbz) minecraft:chest if data block $(cbx) $(cby) $(cbz) LootTable run particle minecraft:campfire_cosy_smoke $(smx) $(smy) $(smz) 0.25 2.5 0.25 0.004 8 force
$execute if block $(cbx) $(cby) $(cbz) minecraft:chest unless data block $(cbx) $(cby) $(cbz) LootTable run function cobblemon_initiative:sidequest/kalahar_hunt/found_warden

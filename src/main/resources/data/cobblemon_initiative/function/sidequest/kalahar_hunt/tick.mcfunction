# Registered in #minecraft:tick. Every 15t, for each ACTIVE hunt: (1) raise a subtle smoke
# column over the buried chest for the player to follow, and (2) check whether the buried chest
# has been opened. Both operations only do anything when the site chunk is loaded — i.e. when the
# player is near — so this is self-gating (no forceload; particles/block-tests no-op while the
# site is unloaded, which is exactly the "follow it once you are close" findability device).
#
# FIND DETECTION: a chest placed with a LootTable NBT drops that tag the instant it is first
# opened (vanilla generates the loot then clears LootTable). So `unless data block <chest>
# LootTable` while the chest block still exists = "the player opened it". KILN retires the hunt
# right there (grab-and-keep). WARDEN keeps its hunt active until Ossa's dialog turn-in files the
# carried Warden's Cache item — but once opened it stops smoking (the cache is in the player's
# hands now, nothing left to follow), so the warden smoke also checks the opened state.
scoreboard players add #hunt_t ci_hunt 1
execute if score #hunt_t ci_hunt matches ..14 run return 0
scoreboard players set #hunt_t ci_hunt 0
execute if score #hunt_kiln ci_hunt matches 1 run function cobblemon_initiative:sidequest/kalahar_hunt/check_kiln
execute if score #hunt_warden ci_hunt matches 1 run function cobblemon_initiative:sidequest/kalahar_hunt/check_warden

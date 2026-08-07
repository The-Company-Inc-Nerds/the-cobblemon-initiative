# MACRO — the warden cache was opened: the player now carries the marked "Warden's Cache" chest
# item. Stop the smoke (nothing left to follow) and mark the hunt awaiting turn-in
# (#hunt_warden = 2 so tick's `matches 1` stops firing this, and Ossa's arm button stays gated
# `matches 1..`). Restore the dug column to plain sand so a later re-arm (after turn-in) buries a
# fresh cache. The hunt COMPLETES at Ossa's dialog turn-in (deposit_warden). Macro args come from
# the storage warden compound via check_warden_apply.
$setblock $(cbx) $(cby) $(cbz) minecraft:sand
scoreboard players set #hunt_warden ci_hunt 2
title @a[distance=..24] actionbar [{"text":"You lift a sealed cache from the sand. Take it to Warden Ossa.","color":"gold"}]
playsound minecraft:block.chest.open master @a[distance=..24] ~ ~ ~ 0.7 0.9

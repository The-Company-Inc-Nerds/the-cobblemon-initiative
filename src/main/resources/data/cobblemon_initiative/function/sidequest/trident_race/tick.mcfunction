# Trident Tide-Race — per-tick guard. Needs #minecraft:tick (added to tick.json).
# Cheap when idle: one selector check, nothing else runs unless a race is live.
execute if entity @a[tag=ci_trident_racing] run function cobblemon_initiative:sidequest/trident_race/tick_run

# Augmented Ring Run — per-tick guard. Needs #minecraft:tick (added to tick.json).
# Cheap when idle: one selector check, nothing else runs unless a race is live.
execute if entity @a[tag=ci_aug_racing] run function cobblemon_initiative:sidequest/augmented_race/tick_run

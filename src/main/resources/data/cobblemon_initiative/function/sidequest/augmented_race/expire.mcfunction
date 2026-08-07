# Augmented Ring Run — timer expired. Every live runner fails: dropped augment, reset counter, and
# teleported back to Arlo's rig deck (the shared fail path — same as a floor fall). Free retries at Arlo.
execute as @a[tag=ci_aug_racing] at @s run function cobblemon_initiative:sidequest/augmented_race/fail
bossbar set cobblemon_initiative:augmented_race visible false

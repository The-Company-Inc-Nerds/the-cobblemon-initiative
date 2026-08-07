# Augmented Ring Run — timer objective + bossbar setup. Needs #minecraft:load (added to load.json).
# Cloned from sidequest/trident_race (the proven ordered-ring timed race), relocated to the Cyber City
# tower cluster off Arlo Datagear's rooftop rig deck. A 37-ring vertical parkour with a Speed II +
# Jump Boost II augment (re-asserted each tick by AugmentedRaceManager) and a crouch-on-land roll that
# negates fall damage (also Java). Idempotent: #init guards the one-time bossbar add.
scoreboard objectives add ci_augrace dummy
scoreboard players set #twenty ci_augrace 20
execute unless score #init ci_augrace matches 1 run bossbar add cobblemon_initiative:augmented_race [{"text":"Augmented Ring Run","color":"light_purple"}]
scoreboard players set #init ci_augrace 1
bossbar set cobblemon_initiative:augmented_race color purple
bossbar set cobblemon_initiative:augmented_race style notched_10
bossbar set cobblemon_initiative:augmented_race visible false
# A relog mid-run abandons the attempt cleanly (tags persist; timer state resets). AugmentedRaceManager
# watches ci_aug_racing — dropping the tag also drops the Speed/Jump effects next tick, so the augment
# never leaks past a relog-abandoned run.
scoreboard players reset @a[tag=ci_aug_racing] ci_augrace_cp
tag @a remove ci_aug_racing
# Per-player ordered-ring counter (0 = none passed, advances 1..37 through the 37 rings).
scoreboard objectives add ci_augrace_cp dummy

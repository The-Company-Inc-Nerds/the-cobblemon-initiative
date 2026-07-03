# Off the Record - the surveillance sensor. Register in #minecraft:tick (orchestrator wires
# the tag). Cheap: every line is gated on a carry/errand tag, so it is a no-op the rest of
# the run. NpcSightManager writes each auditor's can_see_player score every tick; the two
# sweep NPCs are tagged 'auditor' and npcsight-registered at placement (scoreboard IPC).
# Nothing here forces damage - being seen only logs you.

# Re-log cooldown decay.
scoreboard players remove @a[scores={obs_cd=1..}] obs_cd 1

# Carrying a parcel while an auditor has eyes on you -> throttled OBSERVATION LOGGED.
execute as @a[tag=carrying_ledger] at @s if entity @e[tag=auditor,scores={can_see_player=1..},distance=..24] if score @s obs_cd matches ..0 run function cobblemon_initiative:sidequest/off_record/logged
execute as @a[tag=carrying_basket] at @s if entity @e[tag=auditor,scores={can_see_player=1..},distance=..24] if score @s obs_cd matches ..0 run function cobblemon_initiative:sidequest/off_record/logged

# Errand 3 (the inversion): keep seen_by_auditor in sync with the auditors line of sight so
# the auditor dialog can only be logged-on-purpose while actually observed.
execute as @a[tag=off_errand_3_active,tag=!errand3_done] at @s if entity @e[tag=auditor,scores={can_see_player=1..},distance=..24] run tag @s add seen_by_auditor
execute as @a[tag=off_errand_3_active,tag=!errand3_done] at @s unless entity @e[tag=auditor,scores={can_see_player=1..},distance=..24] run tag @s remove seen_by_auditor

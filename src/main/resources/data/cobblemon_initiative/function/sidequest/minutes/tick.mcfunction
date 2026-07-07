# Minutes of the Quarterly Review — branch office poller. Register in #minecraft:tick
# (orchestrator wires the tag). Clone of sidequest/memo/tick.mcfunction, the shipped
# loiter pattern: TICK units, 160-tick (8 s) threshold, EYES ON YOU / CLEAR ladder,
# seen-reset and away-reset — on the namespaced ci_loiter_hz objective (see load).
#
# Sensors: the receptionist and analyst are npcsight-registered and entity-tagged
# hz_office_staff at placement (NpcSightManager writes can_see_player every tick —
# scoreboard-as-IPC, the documented pattern). Chen Bao is entity-tagged
# hz_branch_manager and his body anchors the door-side trigger volume (distance ..6
# of the top-floor lectern) — no trigger box is built, the distance check IS the box.

# One-time approach beat (constraint-safe text).
execute as @a[tag=!hz_office_warned] at @s if entity @e[tag=hz_office_staff,distance=..10] run function cobblemon_initiative:sidequest/minutes/approach_warn

# Off the Org Chart latch: any office-staff sighting marks the visit (sight range 10 is
# the office volume — a can_see_player hit implies the player is inside the cone).
# The LOGGED beat fires exactly once (identical selectors, inserted before the latch).
execute as @a[tag=!hz_minutes_heard,tag=!hz_office_seen] if entity @e[tag=hz_office_staff,scores={can_see_player=1}] run title @s actionbar [{"text":"LOGGED.","color":"red","bold":true},{"text":" Your visit is on the ledger now.","color":"gray"}]
execute as @a[tag=!hz_minutes_heard,tag=!hz_office_seen] if entity @e[tag=hz_office_staff,scores={can_see_player=1}] run tag @s add hz_office_seen

# Leaving the door-side landing resets loiter progress (the 8 s must be continuous).
execute as @a[scores={ci_loiter_hz=1..}] at @s unless entity @e[tag=hz_branch_manager,distance=..6] run title @s actionbar [{"text":"Off the landing.","color":"red"},{"text":" The reading starts over at the door.","color":"gray"}]
execute as @a[scores={ci_loiter_hz=1..}] at @s unless entity @e[tag=hz_branch_manager,distance=..6] run scoreboard players reset @s ci_loiter_hz

# EYES ON YOU / CLEAR meter + loiter countdown at the door for players without the minutes.
execute as @a[tag=!hz_minutes_heard] at @s if entity @e[tag=hz_branch_manager,distance=..6] run function cobblemon_initiative:sidequest/minutes/near_office

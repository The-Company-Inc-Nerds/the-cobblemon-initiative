# Minutes of the Quarterly Review — branch office poller. Register in #minecraft:tick
# (orchestrator wires the tag). Clone of sidequest/memo/tick.mcfunction, the shipped
# loiter pattern: TICK units, 160-tick (8 s) threshold, EYES ON YOU / CLEAR ladder,
# seen-reset and away-reset — on the namespaced ci_loiter_hz objective (see load).
#
# Sensors (alpha.25): THREE hz_office_staff bodies — Ning at ground-floor reception,
# Lan the analyst on the mezzanine (re-tagged; her cone covers the stairwell — the
# ROUND 13E hold-out is reversed, the loiter was uncontested without her), and Anong
# the records clerk (mezzanine, ex-nurse body). NpcSightManager discovers them by tag
# and writes can_see_player every tick (scoreboard-as-IPC, the documented pattern;
# note matchProfile is first-tag-wins — Lan rides her hz_analyst DIALOG profile but
# can_see_player is written for every mode, and this file selects by ENTITY tag).
# ALPHA.26 (N8): the hz_office_staff tag profile is range 14 now (was 10 — the three
# bodies sit 11.0-12.7 blocks from the door landing, so EYES ON YOU could never fire
# there; floors block LOS, so the landing stays winnable by breaking line of sight).
# Chen Bao is entity-tagged hz_branch_manager and his body anchors the door-side
# trigger volume (distance ..6 of the top-floor lectern) — no trigger box is built,
# the distance check IS the box. He is ambient_static as of alpha.26: he NEVER looks
# up from the reading (the alpha.25 head-tracking stare was the bug the playtest
# called backwards) — his staff do the looking for him.

# One-time approach beat (constraint-safe text). Radius tracks the alpha.26 sight
# range 14 so the stealth contract is stated before any sensor can possibly log you.
execute as @a[tag=!hz_office_warned] at @s if entity @e[tag=hz_office_staff,distance=..14] run function cobblemon_initiative:sidequest/minutes/approach_warn

# Off the Org Chart latch: any office-staff sighting marks the visit (sight range 14 as
# of alpha.26 covers the office volume including the door landing — a can_see_player
# hit implies the player is inside a cone).
# The LOGGED beat fires exactly once (identical selectors, inserted before the latch).
execute as @a[tag=!hz_minutes_heard,tag=!hz_office_seen] if entity @e[tag=hz_office_staff,scores={can_see_player=1}] run title @s actionbar [{"text":"LOGGED.","color":"red","bold":true},{"text":" Your visit is on the ledger now.","color":"gray"}]
execute as @a[tag=!hz_minutes_heard,tag=!hz_office_seen] if entity @e[tag=hz_office_staff,scores={can_see_player=1}] run tag @s add hz_office_seen

# ALPHA.25 — the ledger cools: walking well clear of every office sensor (24 blocks)
# clears hz_office_seen, so the Off the Org Chart clean-run bonus is retryable instead
# of a permanent one-strike latch. The bonus now means "the run that HEARD the minutes
# was clean", which matches how the playtest actually replayed the climb.
execute as @a[tag=hz_office_seen,tag=!hz_minutes_heard] at @s unless entity @e[tag=hz_office_staff,distance=..24] run tag @s remove hz_office_seen
execute as @a[tag=hz_office_seen,tag=!hz_minutes_heard] at @s unless entity @e[tag=hz_office_staff,distance=..24] run title @s actionbar [{"text":"The ledger cools.","color":"gray","italic":true},{"text":" Nobody upstairs is still looking.","color":"dark_gray"}]

# Leaving the door-side landing resets loiter progress (the 8 s must be continuous).
execute as @a[scores={ci_loiter_hz=1..}] at @s unless entity @e[tag=hz_branch_manager,distance=..6] run title @s actionbar [{"text":"Off the landing.","color":"red"},{"text":" The reading starts over at the door.","color":"gray"}]
execute as @a[scores={ci_loiter_hz=1..}] at @s unless entity @e[tag=hz_branch_manager,distance=..6] run scoreboard players reset @s ci_loiter_hz

# EYES ON YOU / CLEAR meter + loiter countdown at the door for players without the minutes.
execute as @a[tag=!hz_minutes_heard] at @s if entity @e[tag=hz_branch_manager,distance=..6] run function cobblemon_initiative:sidequest/minutes/near_office

# ALPHA.3 (N1): kick cooldown decay — see minutes/caught.
scoreboard players remove @a[scores={ci_kick_cd=1..}] ci_kick_cd 1

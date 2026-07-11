# Whiteout corridor per-tick work — only called by gym/nifl_whiteout while a player
# is inside the box (band_tags style: every line is a cheap tag/score-gated selector,
# all effects idempotent). Coordinates are showrunner marks — TODO(showrunner):
# confirm the corridor box (3618..3638 / 112..126 / 1901..1934), the south entry
# band (z 1926..1934) and the leader-side marker (3628.5 119 1903.5, r 2.5).

# Taunt cooldown decay (60 = one taunt every 3s while continuously seen).
scoreboard players remove @a[scores={nifl_wo=1..}] nifl_wo 1

# Ambient blizzard wash around each player in the corridor.
execute as @a[x=3618,y=112,z=1901,dx=20,dy=14,dz=33] at @s run particle minecraft:snowflake ~ ~1.2 ~ 2.5 1.4 2.5 0.01 6 normal

# Cold breathing off the sentinel statues.
execute as @e[tag=nifl_sentry] at @s run particle minecraft:snowflake ~ ~1.6 ~ 0.3 0.4 0.3 0.005 2 normal

# Arm a fresh crossing at the south (start) end. A new run starts unseen — a stale
# nifl_seen from an abandoned half-crossing is wiped at the arming moment only.
execute as @a[x=3618,y=112,z=1926,dx=20,dy=14,dz=8,tag=!nifl_crossing] run tag @s remove nifl_seen
execute as @a[x=3618,y=112,z=1926,dx=20,dy=14,dz=8,tag=!nifl_crossing] run tag @s add nifl_crossing

# Arrivals at the leader-side marker. Order is load-bearing: the clean grant runs
# BEFORE the quiet seen-reset, so a seen arrival cannot reset-then-grant in one tick;
# the crossing disarms last (loitering at the marker never re-fires anything).
execute as @a[x=3628.5,y=119,z=1903.5,distance=..2.5,tag=nifl_crossing,tag=!nifl_seen,tag=!nifl_whiteout_clear] at @s run function cobblemon_initiative:gym/nifl_whiteout_cleared
execute as @a[x=3628.5,y=119,z=1903.5,distance=..2.5,tag=nifl_crossing,tag=nifl_seen] run tag @s remove nifl_seen
execute as @a[x=3628.5,y=119,z=1903.5,distance=..2.5,tag=nifl_crossing] run tag @s remove nifl_crossing

# A sentinel with eyes open: tax the seen player. can_see_player is the NPC-side
# raycast score (NpcSightManager, scoreboard IPC — off_record consumer precedent);
# distance ..13 = profile range 12 + slack. Single-player: the nearest corridor
# player IS the seen player. Throttled by nifl_wo (unset score passes the unless).
# The tax retires once Boreas falls; the statues keep watching, the snow keeps falling.
execute as @a[x=3618,y=112,z=1901,dx=20,dy=14,dz=33,tag=!defeated_nifl_leader] at @s unless score @s nifl_wo matches 1.. if entity @e[tag=nifl_sentry,scores={can_see_player=1..},distance=..13] run function cobblemon_initiative:gym/nifl_whiteout_seen

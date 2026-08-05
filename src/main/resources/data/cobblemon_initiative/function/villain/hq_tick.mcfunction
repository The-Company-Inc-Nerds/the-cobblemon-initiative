# Company HQ access enforcement — tick (playtest 2026-08-05 N11-N13/P2-P9). Registered in
# data/minecraft/tags/function/tick.json. Grant first, then two escort boxes on the built
# tower, then cooldown decay:
#   (a) GROUND BOX x1608..1633 y88..100 z1090..1115 — a player who is not raid-eligible
#       (fields_liberated < 6 OR memory_fragment < 7; memory_fragment IS badge count) is
#       walked out by security (villain/hq_bounce). The door-guard pair (ci_hq_guard,
#       hq_security_voss/kessler at z1116.5) is the fiction; this box is the mechanic.
#       Lobby agents (cipher/flux/grid/pulse) become reachable exactly at eligibility.
#   (b) PENTHOUSE BOX x1608..1636 y168..200 z1086..1118 — sealed until hq_basement_cleared
#       (villain/hq_bounce_penthouse walks the player back to the ground-floor lift).
#       Victor Node holds the lift landing (y171) as the spoken half of the same gate.
# Escorts skip creative/spectator (dev/camera work must never be tp'd) and respect the
# shared 200t ci_hq_kick_cd so re-entry is not an instant tp loop (minutes/caught shape).
# Both not-eligible conditions get their own line; after the first bounce sets the
# cooldown, the second line's cooldown guard fails — a doubly-ineligible player is
# escorted exactly once per window.

# (d) basement-cleared grant — BEFORE the boxes, so a same-tick grant can never lose the
# race and eat one spurious escort. COO Noir (villain_admin_commander) is the basement
# boss; his defeat tag opens the penthouse.
tag @a[tag=!hq_basement_cleared,tag=defeated_villain_admin_commander] add hq_basement_cleared
# ── TEMP until the basement cast is pinned — REMOVE the eligibility grant below when
# villain_admin_commander gets his placement (COO Noir has no pin as of 2026-08-05).
# Without a placeable boss, gating the penthouse on his defeat tag would BRICK the DJ
# finale; until the pin lands, raid eligibility itself opens the penthouse.
execute as @a[tag=!hq_basement_cleared] if score @s fields_liberated matches 6.. if score @s memory_fragment matches 7.. run tag @s add hq_basement_cleared

# (a) ground box: not raid-eligible -> escorted out.
execute as @a[x=1608,y=88,z=1090,dx=25,dy=12,dz=25,gamemode=!creative,gamemode=!spectator] at @s unless score @s ci_hq_kick_cd matches 1.. unless score @s fields_liberated matches 6.. run function cobblemon_initiative:villain/hq_bounce
execute as @a[x=1608,y=88,z=1090,dx=25,dy=12,dz=25,gamemode=!creative,gamemode=!spectator] at @s unless score @s ci_hq_kick_cd matches 1.. unless score @s memory_fragment matches 7.. run function cobblemon_initiative:villain/hq_bounce

# (b) penthouse box: basement not cleared -> walked back to the lift, then the street.
execute as @a[x=1608,y=168,z=1086,dx=28,dy=32,dz=32,tag=!hq_basement_cleared,gamemode=!creative,gamemode=!spectator] at @s unless score @s ci_hq_kick_cd matches 1.. run function cobblemon_initiative:villain/hq_bounce_penthouse

# (c) cooldown decay.
scoreboard players remove @a[scores={ci_hq_kick_cd=1..}] ci_hq_kick_cd 1

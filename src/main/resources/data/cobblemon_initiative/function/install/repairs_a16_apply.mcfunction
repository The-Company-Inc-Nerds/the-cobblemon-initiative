# repairs wave a16 — apply: reconcile Victor to the descent design (see repairs_a16_arm).
# Skip entirely if the reveal already ran (Victini exists or joined) — that run is complete.
# SP: @a[the five gates] present <=> the player has qualified for the transform.

# ── Case A: player QUALIFIED but not transformed → he is meant to be DOWN at the path now.
# Never spawn him back up the tower (#amb_victor = 1) and mark the player descended so the
# transform dialog unlocks (the ambient/tick self-heal clears any stray tower body).
execute if entity @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] run scoreboard players set #amb_victor ci_ambient 1
execute as @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] run tag @s add victor_descended
# Adopt the a15 path body if it exists (consume the path latch); otherwise leave the path latch
# PENDING (0) so victor_arrive_path spawns a fresh body when the player next nears the path.
execute if entity @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] if entity @e[type=easy_npc:humanoid,tag=victor_apprentice,x=2536.5,y=106,z=2900.5,distance=..8] run scoreboard players set #amb_victor_path ci_ambient 1
execute if entity @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] unless entity @e[type=easy_npc:humanoid,tag=victor_apprentice,x=2536.5,y=106,z=2900.5,distance=..8] run scoreboard players set #amb_victor_path ci_ambient 0

# ── Case B: player NOT yet qualified → Victor belongs back UP on the tower. Kill the a15 path
# body (and any stray tower body), then reset both latches so the tower latch re-spawns him up
# top on the next visit and clear any stray descended tag.
execute unless entity @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] positioned 2536.5 106 2900.5 run kill @e[type=easy_npc:humanoid,tag=victor_apprentice,distance=..8]
execute unless entity @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] positioned 2522.5 131 2815.5 run kill @e[type=easy_npc:humanoid,tag=victor_apprentice,distance=..8]
execute unless entity @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] run scoreboard players set #amb_victor ci_ambient 0
execute unless entity @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] run scoreboard players set #amb_victor_path ci_ambient 0
execute unless entity @a[tag=victor_hint,tag=docs_filed,tag=lane_done,tag=census_refused,tag=bought_magikarp] unless entity @e[tag=victor_victini,type=!minecraft:player] unless entity @a[tag=victini_joined] run tag @a remove victor_descended

forceload remove 2512 2800
forceload remove 2512 2816
forceload remove 2528 2800
forceload remove 2528 2816
forceload remove 2528 2888
forceload remove 2544 2888
forceload remove 2528 2900
forceload remove 2544 2900
execute if score #debug ci_ambient matches 1 run tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a16: Victor returns to the grain tower — he now descends to the path only once you have earned his transform.","color":"gray"}]

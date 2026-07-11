# One-shot lift for Leader Cicada's hovering perch (called from gym/cicada_float once
# the body's chunk is loaded). Order matters: import the preset FIRST — Entity.load at
# the end of the import applies the root NoGravity:1b baked by the character's
# `float: true` — THEN tp the body up to the perch, where no-gravity holds it. Without
# the import-first, a pre-float world body (NoGravity 0b) would just fall back down.
execute as c577141c-305f-4c20-a6bb-444d3b4d5ae0 at @s run easy_npc preset import data easy_npc:preset/humanoid/takehara_leader.npc.snbt ~ ~ ~ c577141c-305f-4c20-a6bb-444d3b4d5ae0
tp c577141c-305f-4c20-a6bb-444d3b4d5ae0 2055.5 173.0 2460.5
scoreboard players set #cicada_lift ci_gym 1

# repairs wave a2 — arm: mark done, load every affected site, schedule the apply
# pass 3s out (forceloaded chunks need a tick or two before their ENTITIES exist —
# a same-tick kill silently no-ops, which is exactly the bug class this repairs).
scoreboard players set #repair_a2 ci_ambient 1

# Takehara gym tower (old trainer cluster spots)
forceload add 2030 2430 2095 2500
# Sango auditors (posts unchanged; bodies must re-latch to pick up Navigation.Home)
forceload add 2600 2780 2620 2800
forceload add 2570 2930 2590 2950
# old mew-wisp site (buried at y64 under the Safari clearing)
forceload add 1290 1440 1310 1460
# old Oasis pump-crew coords (buried under the lake; bodies likely never spawned)
forceload add 1720 4255 1740 4275

schedule function cobblemon_initiative:install/repairs_a2_apply 60t
execute if score #debug ci_ambient matches 1 run tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Applying one-time world repairs (relocated NPCs)…","color":"gray"}]

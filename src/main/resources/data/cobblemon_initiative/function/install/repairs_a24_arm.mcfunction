# repairs wave a24 — arm: 0.7.0-alpha.7 playtest wave.
# Scope: Kyogre Warning Buoy moved 234/65/2347 -> 259.5/62/2351.5 (the Kyogre arena center + body
# spawn moved to the water at 270/62/2351, so the noble rises out of the harbour behind the buoy).
# Re-latch the buoy for existing worlds.
scoreboard players set #repair_a24 ci_ambient 1
forceload add 228 2341 266 2358
schedule function cobblemon_initiative:install/repairs_a24_apply 3s

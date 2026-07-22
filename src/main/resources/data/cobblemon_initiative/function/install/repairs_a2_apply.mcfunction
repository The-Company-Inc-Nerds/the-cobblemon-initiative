# repairs wave a2 — apply: chunks are live now; sweep the stale bodies, re-arm their
# latches, release the chunks. Latches respawn each NPC at its CURRENT authored coords
# on the next player visit (within 40 blocks).

# Takehara trainers (moved to the greenhouses)
kill @e[type=easy_npc:humanoid,x=2083,y=138,z=2466,distance=..3]
kill @e[type=easy_npc:humanoid,x=2041,y=151,z=2441,distance=..3]
kill @e[type=easy_npc:humanoid,x=2070,y=151,z=2491,distance=..3]
kill @e[type=easy_npc:humanoid,x=2073,y=169,z=2464,distance=..3]
scoreboard players set #amb_takehara_trainer_1 ci_ambient 0
scoreboard players set #amb_takehara_trainer_2 ci_ambient 0
scoreboard players set #amb_takehara_trainer_3 ci_ambient 0
scoreboard players set #amb_takehara_trainer_4 ci_ambient 0

# Sango auditors (same posts; re-latch applies the new patrol Home leash)
kill @e[type=easy_npc:humanoid,x=2611,y=110,z=2792,distance=..14]
kill @e[type=easy_npc:humanoid,x=2578,y=108,z=2942,distance=..14]
scoreboard players set #amb_auditor_a ci_ambient 0
scoreboard players set #amb_auditor_b ci_ambient 0

# Mew-wisp giver (was buried at y64 under the Safari clearing; now at the Oasis)
kill @e[type=easy_npc:humanoid,x=1300,y=64,z=1450,distance=..8]
scoreboard players set #amb_noble_giver_mew_wisp ci_ambient 0

# Oasis pump crew (authored under the lake; almost certainly never spawned — sweep
# defensively, then re-arm so the north-shore spots latch on first visit)
kill @e[type=easy_npc:humanoid,x=1731,y=66,z=4264,distance=..10]
scoreboard players set #amb_agent_pump_foreman ci_ambient 0
scoreboard players set #amb_agent_pump_officer ci_ambient 0
scoreboard players set #amb_oasis_pump_manifold ci_ambient 0

forceload remove 2030 2430 2095 2500
forceload remove 2600 2780 2620 2800
forceload remove 2570 2930 2590 2950
forceload remove 1290 1440 1310 1460
forceload remove 1720 4255 1740 4275
execute if score #debug ci_ambient matches 1 run tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"World repairs applied — relocated NPCs will reappear at their new posts as you travel.","color":"gray"}]

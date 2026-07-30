# Deepcore dojo PVP driver (a22, playtest N5-N10 + "make the fighting gym actual minecraft fights").
# Registered in #minecraft:tick. Orc-camp idiom: each floor + the pit is a 0/1/2 latch on ci_gym,
# every line behind ONE score check so idle state costs a single failed test. LETHALITY is governed by
# DojoConfig.knockoutMode (0.7.0-alpha.10, ModMenu, DEFAULT ON = non-lethal): ON, a defeated fighter is
# left lying at the spot and a player a fighter would kill is knocked out (half a heart + CobbleDollars,
# DojoKnockoutManager); OFF, the old FULLY LETHAL path stands (real damage, hardcore permadeath ->
# PokeballDeathScreen). Bruno (the leader) stays a Cobblemon battle — only the floor masters + pit are PVP.
#
# FLOOR MASTERS (dc_track_full). RAISE on approach (chunk-safe): latch 0 + a whole-dojo player within
# 40 of the post -> spawn the hostile (dojo_raise_N sets latch 1 first). CLEARED poll: latch 1 + a
# player near the post (chunk-load guard — unloaded PersistenceRequired bodies are invisible to
# selectors, so polling without a nearby player false-clears) + no hostile within 24 -> credit
# (dojo_clear_N sets latch 2). Selectors typed easy_npc:humanoid (never bare @e — house law).
execute if score #dc_floor_1 ci_gym matches 0 if entity @a[tag=dc_track_full,x=1016.0,y=129,z=3158.3,distance=..40] positioned 1016.0 129 3158.3 run function cobblemon_initiative:gym/dojo_raise_1
execute if score #dc_floor_1 ci_gym matches 1 if entity @a[x=1016.0,y=129,z=3158.3,distance=..48] positioned 1016.0 129 3158.3 unless entity @e[type=easy_npc:humanoid,tag=dc_floor_1_hostile] run function cobblemon_initiative:gym/dojo_clear_1
execute if score #dc_floor_2 ci_gym matches 0 if entity @a[tag=dc_track_full,x=958.4,y=129,z=3156.7,distance=..40] positioned 958.4 129 3156.7 run function cobblemon_initiative:gym/dojo_raise_2
execute if score #dc_floor_2 ci_gym matches 1 if entity @a[x=958.4,y=129,z=3156.7,distance=..48] positioned 958.4 129 3156.7 unless entity @e[type=easy_npc:humanoid,tag=dc_floor_2_hostile] run function cobblemon_initiative:gym/dojo_clear_2
execute if score #dc_floor_3 ci_gym matches 0 if entity @a[tag=dc_track_full,x=965.8,y=141.5,z=3187.7,distance=..40] positioned 965.8 141.5 3187.7 run function cobblemon_initiative:gym/dojo_raise_3
execute if score #dc_floor_3 ci_gym matches 1 if entity @a[x=965.8,y=141.5,z=3187.7,distance=..48] positioned 965.8 141.5 3187.7 unless entity @e[type=easy_npc:humanoid,tag=dc_floor_3_hostile] run function cobblemon_initiative:gym/dojo_clear_3
execute if score #dc_floor_4 ci_gym matches 0 if entity @a[tag=dc_track_full,x=965.9,y=141.5,z=3183.6,distance=..40] positioned 965.9 141.5 3183.6 run function cobblemon_initiative:gym/dojo_raise_4
execute if score #dc_floor_4 ci_gym matches 1 if entity @a[x=965.9,y=141.5,z=3183.6,distance=..48] positioned 965.9 141.5 3183.6 unless entity @e[type=easy_npc:humanoid,tag=dc_floor_4_hostile] run function cobblemon_initiative:gym/dojo_clear_4
#
# THE PIT (dc_pit_ready = pit-track chosen OR full-track with all 4 floors down; derived per tick in
# gym/deepcore_tower). Stage 0 -> raise Striker on approach; stage 1 (Striker down) -> Ken; stage 2
# (Ken down) -> cleared. Pit center ~990/129/3174 for the poll (Striker 997.7, Ken 984.3).
# Whole-dojo challengers must beat all four floor masters before the pit opens (dc_pit_ready needs
# deepcore_tower>=4). A full-track player who reaches the pit early gets a nudge, not a spawn.
execute if score #dc_pit_stage ci_gym matches 0 as @a[tag=dc_track_full,x=990,y=129,z=3174,distance=..24] if score @s deepcore_tower matches ..3 run title @s actionbar [{"text":"The pit stays shut — drop all four floor masters first","color":"gray"}]
execute if score #dc_pit_stage ci_gym matches 0 if entity @a[tag=dc_pit_ready,x=997.7,y=129,z=3174.5,distance=..40] positioned 997.7 129 3174.5 run function cobblemon_initiative:gym/dojo_pit_striker
execute if score #dc_pit_stage ci_gym matches 1 if entity @a[x=990,y=129,z=3174,distance=..48] positioned 990 129 3174 unless entity @e[type=easy_npc:humanoid,tag=dc_striker_hostile] run function cobblemon_initiative:gym/dojo_pit_ken
execute if score #dc_pit_stage ci_gym matches 2 if entity @a[x=990,y=129,z=3174,distance=..48] positioned 990 129 3174 unless entity @e[type=easy_npc:humanoid,tag=dc_ken_hostile] run function cobblemon_initiative:gym/dojo_pit_done

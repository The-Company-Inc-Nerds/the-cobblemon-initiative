# Dojo floor master 2 CLEARED (a22 PVP) — absence poll in gym/deepcore_pvp_tick found no hostile
# (dc_floor_2_hostile) near the post. Latch 1->2 FIRST (never re-credits), then grant the
# floor-clear tag that drives deepcore_tower (the gte-4 count that weakens Bruno) and dc_pit_ready.
# Single-player: @a is the one challenger. Position-independent tag, safe.
scoreboard players set #dc_floor_2 ci_gym 2
tag @a add defeated_deepcore_trainer_2
title @a[distance=..48] actionbar [{"text":"Battle Girl Mika is down","color":"gold"}]
playsound minecraft:block.anvil_land master @a[distance=..48] 958.4 129 3156.7 0.6 1.4

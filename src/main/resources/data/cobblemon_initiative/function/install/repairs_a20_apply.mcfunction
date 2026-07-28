# repairs wave a20 — apply (0.7.0-alpha.2 playtest wave). a18/a19 kill shapes.
# Typed kills only (a13/a14 lesson): vows are type=easy_npc:allay, Aurora is
# type=easy_npc:fairy, market humanoids are type=easy_npc:humanoid, Cloud is the
# tag-keyed cobblemon_npc.

# Linh Hua — uuid tp to the new stall row (bundled-map nbt moved too; this heals
# already-shipped worlds)
tp 1c06d60f-a97e-49a0-b0e6-fede9deaa00e 1544.5 86.0 2048.5

# Bo Huan — re-latch at the market corner (old cart spot; nearest humanoid is
# Auntie Song 27 blocks north — r3 safe. Cloud's new spot is 10 blocks off AND
# type-excluded)
execute positioned 1512.5 84.0 2085.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_hz_trader_apricorns ci_ambient 0

# Cloud — tag-kill catches the strayed body wherever it stands (a17 precedent)
kill @e[type=easy_npc:cobblemon_npc,tag=ci_amb_companion_wooloo]
scoreboard players set #amb_wooloo ci_ambient 0

# Rong — uuid tp onto the loft, yaw -151 (cone NNE over the lectern + stair landing;
# the south-wall shadow is the sneak lane). Belt-and-braces sensor tag: the a20
# preset refresh also addTags it, but the tp must not outrun the sensor.
tp f45f3fda-5de3-4f69-9485-e4a8f5df225f 1533.5 99.0 2000.5 -151 0
tag f45f3fda-5de3-4f69-9485-e4a8f5df225f add hz_office_staff

# Retro-heal the alpha.1 in_trainer_battle leak (BATTLE_FLED never cleared it, so a
# fled forced battle permanently disarmed every pursue trainer; the JOIN-time clear
# guards from here on, this line heals players already online when repairs run)
tag @a remove in_trainer_battle

# Fen-Nurse Wisteria — character CUT (Liora Starquill is the marsh nurse, on the
# Center body). Positioned kill, NO latch reset (her placement row is gone);
# holder hygiene per a19.
execute positioned 1068.5 65.0 2465.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players reset #amb_mm_nurse ci_ambient

# Vows 1-4 — re-latch one block higher (kills at the OLD authored coords; r3 absorbs
# allay hover drift, and the allay type filter can only ever hit vows)
execute positioned 943.4 40.0 2644.5 run kill @e[type=easy_npc:allay,distance=..3]
scoreboard players set #amb_fairy_allay_1 ci_ambient 0
execute positioned 949.2 24.0 2666.5 run kill @e[type=easy_npc:allay,distance=..3]
scoreboard players set #amb_fairy_allay_2 ci_ambient 0
execute positioned 979.3 22.0 2682.5 run kill @e[type=easy_npc:allay,distance=..3]
scoreboard players set #amb_fairy_allay_3 ci_ambient 0
execute positioned 921.1 4.0 2698.9 run kill @e[type=easy_npc:allay,distance=..3]
scoreboard players set #amb_fairy_allay_4 ci_ambient 0

# The Fifth Vow — character CUT (resolve is sworn to Aurora at the floor).
# Positioned kill, NO latch reset (placement row gone); holder hygiene per a19.
execute positioned 928.5 0.0 2716.2 run kill @e[type=easy_npc:allay,distance=..3]
scoreboard players reset #amb_fairy_allay_5 ci_ambient

# Aurora — re-latch at y1.5 with float + scale (fairy-typed kill: any world that ran
# a18 has her NEW fairy body at the floor spot)
execute positioned 947.5 0.0 2703.8 run kill @e[type=easy_npc:fairy,distance=..3]
scoreboard players set #amb_fairy_shrine_leader ci_ambient 0

forceload remove 1495 1980 1550 2095
forceload remove 1060 2457 1076 2473
forceload remove 916 2640 984 2724

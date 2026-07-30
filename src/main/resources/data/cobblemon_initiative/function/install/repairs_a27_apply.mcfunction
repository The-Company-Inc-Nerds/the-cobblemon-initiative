# repairs wave a27 — apply (chunks live, 3s after arm).
# ── Gym eyesight trainers: kill the stale talk-to-battle bodies at their OLD spawn coords
#    (placement + 0.5 block-centre) + reset the latches so the recompiled pursue presets re-spawn
#    at the new ring positions. Radius 2 keeps Leader Neptune (stand ~596/87/3646, >3 blocks off
#    the cluster) safe from the sweep. ──
execute positioned 594.5 87 3649.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 598.5 87 3643.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 593.5 87 3644.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 599.5 87 3644.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 593.5 87 3648.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 599.5 87 3648.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players set #amb_gaviota_jr_apprentice ci_ambient 0
scoreboard players set #amb_gaviota_apprentice ci_ambient 0
scoreboard players set #amb_gaviota_trainer_1 ci_ambient 0
scoreboard players set #amb_gaviota_trainer_2 ci_ambient 0
scoreboard players set #amb_gaviota_trainer_3 ci_ambient 0
scoreboard players set #amb_gaviota_trainer_4 ci_ambient 0
# ── uuid civilians tp to their a11 spots (Paolo pen 1, Alessia pen 2, Vittorio north pier) ──
tp afd87dc0-ff01-4363-9f49-7ee972f939dd 552.5 87 3504.5
tp f85cee27-f6a8-4114-a707-fafdc94ac59c 546.5 87 3478.5
tp da39ea5e-f933-4713-adc4-da7cf9460e5a 559.5 103 3649.5
# ── teardown ──
forceload remove 550 3626 630 3666
forceload remove 545 3476 558 3506

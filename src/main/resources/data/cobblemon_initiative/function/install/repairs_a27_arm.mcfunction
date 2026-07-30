# repairs wave a27 — arm: 0.7.0-alpha.11 Gaviota water-gym playtest wave.
# Scope: the 6 gym trainers become EYESIGHT (pursue) battles and move onto the flooded-arena
# ring (jr_apprentice/apprentice/trainer_1-4 -> new coords + gaviota_sight_* tags baked in), so
# their OLD stale talk-to-battle bodies must die + their latches reset to re-spawn from the
# recompiled pursue presets. Plus the a11 civilian nudges: Paolo/Alessia daycare pens (uuid),
# Vittorio north-pier watch post (uuid). Forceload old + new sites so the apply kill/tp never
# no-ops on an unloaded chunk.
scoreboard players set #repair_a27 ci_ambient 1
forceload add 550 3626 630 3666
forceload add 545 3476 558 3506
schedule function cobblemon_initiative:install/repairs_a27_apply 3s

# repairs wave a22 — apply (chunks live, 3s after arm). Kill stale bodies + reset placement latches
# so the a22 presets re-spawn on next approach.
# ── Deepcore gym ──────────────────────────────────────────────────────────────────
# Floor masters + pit passives — TAG kill (old ambient_wander bodies drift off the post) + latch
# reset. New passive PVP-flavor bodies re-spawn within 40 blocks. The dojo PVP latches themselves
# (#dc_floor_*/#dc_pit_stage) are NOT touched here — gym/load init-if-unset owns them.
kill @e[type=easy_npc:humanoid,tag=dc_floor_1_body]
kill @e[type=easy_npc:humanoid,tag=dc_floor_2_body]
kill @e[type=easy_npc:humanoid,tag=dc_floor_3_body]
kill @e[type=easy_npc:humanoid,tag=dc_floor_4_body]
kill @e[type=easy_npc:humanoid,tag=dc_jr_body]
kill @e[type=easy_npc:humanoid,tag=dc_pit_lead]
scoreboard players set #amb_deepcore_trainer_1 ci_ambient 0
scoreboard players set #amb_deepcore_trainer_2 ci_ambient 0
scoreboard players set #amb_deepcore_trainer_3 ci_ambient 0
scoreboard players set #amb_deepcore_trainer_4 ci_ambient 0
scoreboard players set #amb_deepcore_jr_apprentice ci_ambient 0
scoreboard players set #amb_deepcore_apprentice ci_ambient 0
# ── Gaviota west quay ─────────────────────────────────────────────────────────────
# Zwiggo Man -> Deckhand Rocko (stationary, positioned kill at the quay lip) + re-latch.
execute positioned 410.7 64.0 3501.4 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_zwiggo_man ci_ambient 0
# Zwiggo swampert: kill any old summoned body; re-arm the new quay latch ONLY if not yet recruited
# (a recruited player must not get a second recruitable body — ambient/tick also self-heals this).
kill @e[type=easy_npc:cobblemon_npc,tag=zwiggo_body]
execute unless entity @a[tag=zwiggo_joined] run scoreboard players set #amb_zwiggo_swampert ci_ambient 0
# ── teardown ──────────────────────────────────────────────────────────────────────
forceload remove 952 3150 1020 3195
forceload remove 400 3494 416 3508

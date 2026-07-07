# takehara_trainer_1 — latch FIRST; reset #amb_takehara_trainer_1 to 0 (+ kill the body) to respawn (re-rolls).
# spawn_variants roll (review B7): one per-world `random value 1..2` picks roster
# A (takehara_trainer_1) or B (takehara_trainer_1_b) — same displayName/coords/defeat tag, different team.
scoreboard players set #amb_takehara_trainer_1 ci_ambient 1
execute store result score #var_takehara_trainer_1 ci_ambient run random value 1..2
execute if score #var_takehara_trainer_1 ci_ambient matches 1 run easy_npc preset import_new data easy_npc:preset/humanoid/takehara_trainer_1.npc.snbt 2055.5 138 2428.5
execute if score #var_takehara_trainer_1 ci_ambient matches 2 run easy_npc preset import_new data easy_npc:preset/humanoid/takehara_trainer_1_b.npc.snbt 2055.5 138 2428.5

# Deep pit, phase 2 — Striker is down, Apprentice Ken CLOSES (a22 PVP, playtest N10: "melee pvp
# after jr is defeated"). Fired from gym/deepcore_pvp_tick when stage 1 finds no Striker
# (dc_striker_hostile) near the pit. Latch 1->2 FIRST, credit Striker's defeat tag, then kill the
# passive Ken flavor body and spawn the melee duel body (duel_melee, fists). Player is present
# (just felled Striker) so the chunk is live — no forceload needed. Ken's death -> dojo_pit_done.
scoreboard players set #dc_pit_stage ci_gym 2
tag @a add defeated_deepcore_jr_apprentice
kill @e[type=easy_npc:humanoid,tag=dc_pit_lead]
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_pit_ken.npc.snbt 984.3 129 3173.2
title @a[distance=..48] subtitle [{"text":"Ken steps in — fists now, no range to hide behind","color":"red"}]
playsound minecraft:entity.player.attack.sweep master @a[distance=..48] 984.3 129 3173.2 1 0.9

# Deep pit, phase 1 — Jr. Apprentice Striker RAISES (a22 PVP, playtest N9: "attack with range
# attacks"). Fired from gym/deepcore_pvp_tick when a pit-ready player (dc_pit_ready = pit-track
# chosen, OR full-track with all four floor masters down) walks within 40 of the pit. Latch
# 0->1 FIRST (one-shot), kill the passive Striker flavor body, spawn the ranged duel body
# (duel_ranged: holds a bow, BOW_ATTACK, squishier). His death is caught by gym/dojo_pit_ken.
scoreboard players set #dc_pit_stage ci_gym 1
# a22 KO reset: claim the ambient latch too — gym/dojo_reset re-arms it (0), and without this a
# same-tick ambient/placements pass could stand the passive twin next to the raised hostile.
scoreboard players set #amb_deepcore_jr_apprentice ci_ambient 1
kill @e[type=easy_npc:humanoid,tag=dc_jr_body]
# a22 KO reset: sweep the old knocked-out sleeper at this post so a re-raised Striker's corpse vanishes.
execute positioned 997.7 129 3174.5 run kill @e[type=easy_npc:humanoid,tag=dc_knocked,distance=..6]
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_pit_striker.npc.snbt 997.7 129 3174.5
title @a[distance=..40] title [{"text":"THE PIT","color":"dark_red","bold":true}]
title @a[distance=..40] subtitle [{"text":"Striker opens at range — close the distance","color":"red"}]
playsound minecraft:entity.arrow.shoot master @a[distance=..40] 997.7 129 3174.5 1 1.2

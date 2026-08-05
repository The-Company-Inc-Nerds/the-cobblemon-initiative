# Dojo floor master 3 RAISES (a22 PVP, playtest N5-N8). Fired from gym/deepcore_pvp_tick when a
# 'Face the whole dojo' (dc_track_full) player walks within 40 of the post — chunk guaranteed
# live (orc camp raise idiom). Latch 0->1 FIRST (one-shot), kill the passive flavor body, spawn
# the hostile duel body (Martial Artist Kenji) at the post. It punches with fists (duel_melee, FULLY LETHAL per
# the showrunner ruling). Its death is caught by gym/dojo_clear_3.
scoreboard players set #dc_floor_3 ci_gym 1
# a22 KO reset: claim the ambient latch too — gym/dojo_reset re-arms it (0), and without this a
# same-tick ambient/placements pass could stand the passive twin next to the raised hostile.
scoreboard players set #amb_deepcore_trainer_3 ci_ambient 1
kill @e[type=easy_npc:humanoid,tag=dc_floor_3_body]
# a22 KO reset: sweep the old knocked-out sleeper at this post so a re-raised master's corpse vanishes.
execute positioned 965.8 141.5 3187.7 run kill @e[type=easy_npc:humanoid,tag=dc_knocked,distance=..6]
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_duelist_3.npc.snbt 965.8 141.5 3187.7
title @a[distance=..40] actionbar [{"text":"Martial Artist Kenji squares up — no Poke Balls down here","color":"red"}]
playsound minecraft:entity.player.attack.strong master @a[distance=..40] 965.8 141.5 3187.7 1 0.9

# Dojo floor master 4 RAISES (a22 PVP, playtest N5-N8). Fired from gym/deepcore_pvp_tick when a
# 'Face the whole dojo' (dc_track_full) player walks within 40 of the post — chunk guaranteed
# live (orc camp raise idiom). Latch 0->1 FIRST (one-shot), kill the passive flavor body, spawn
# the hostile duel body (Capoeira Rio) at the post. It punches with fists (duel_melee, FULLY LETHAL per
# the showrunner ruling). Its death is caught by gym/dojo_clear_4.
scoreboard players set #dc_floor_4 ci_gym 1
kill @e[type=easy_npc:humanoid,tag=dc_floor_4_body]
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_duelist_4.npc.snbt 965.9 141.5 3183.6
title @a[distance=..40] actionbar [{"text":"Capoeira Rio squares up — no Poke Balls down here","color":"red"}]
playsound minecraft:entity.player.attack.strong master @a[distance=..40] 965.9 141.5 3183.6 1 0.9

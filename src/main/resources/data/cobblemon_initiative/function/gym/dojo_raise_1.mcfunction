# Dojo floor master 1 RAISES (a22 PVP, playtest N5-N8). Fired from gym/deepcore_pvp_tick when a
# 'Face the whole dojo' (dc_track_full) player walks within 40 of the post — chunk guaranteed
# live (orc camp raise idiom). Latch 0->1 FIRST (one-shot), kill the passive flavor body, spawn
# the hostile duel body (Black Belt Ryu) at the post. It punches with fists (duel_melee, FULLY LETHAL per
# the showrunner ruling). Its death is caught by gym/dojo_clear_1.
scoreboard players set #dc_floor_1 ci_gym 1
kill @e[type=easy_npc:humanoid,tag=dc_floor_1_body]
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_duelist_1.npc.snbt 1016.0 129 3158.3
title @a[distance=..40] actionbar [{"text":"Black Belt Ryu squares up — no Poke Balls down here","color":"red"}]
playsound minecraft:entity.player.attack.strong master @a[distance=..40] 1016.0 129 3158.3 1 0.9

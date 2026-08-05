# Deepcore dojo FULL RESET (0.7.0-alpha.22, playtest note 2: a knocked-out player is ejected and
# the gym resets). Fired by DojoKnockoutManager on the tick after a player KO, AFTER it has
# discard()ed every live hostile in Java (dc_floor_1..4_hostile / dc_striker_hostile /
# dc_ken_hostile) — /kill-ing a hostile here would re-enter onAfterDeath and drop a fresh corpse;
# discard fires no death event. Must run while the dojo chunks are loaded (the KO'd player is
# still on the mat — the eject teleport is issued after this function).
# RULING: a knockout resets the WHOLE run — beaten masters get back up (defeat tags stripped,
# passive flavor bodies return to their posts on approach). KEPT: Bruno's own credit
# (defeated_deepcore_leader) and the chosen track (dc_track_full / dc_track_pit).
#
# Corpse sweep — every knocked-out sleeper body left lying in the dojo.
kill @e[type=easy_npc:humanoid,tag=dc_knocked]
# Re-arm the floor + pit latches (0 = armed; gym/deepcore_pvp_tick raises on next approach).
scoreboard players set #dc_floor_1 ci_gym 0
scoreboard players set #dc_floor_2 ci_gym 0
scoreboard players set #dc_floor_3 ci_gym 0
scoreboard players set #dc_floor_4 ci_gym 0
scoreboard players set #dc_pit_stage ci_gym 0
# Strip the run's defeat credits (deepcore_tower + dc_pit_ready recompute from these per tick in
# gym/deepcore_tower; the band tags follow in dialog/band_tags).
tag @a remove defeated_deepcore_trainer_1
tag @a remove defeated_deepcore_trainer_2
tag @a remove defeated_deepcore_trainer_3
tag @a remove defeated_deepcore_trainer_4
tag @a remove defeated_deepcore_jr_apprentice
tag @a remove defeated_deepcore_apprentice
# Passive flavor bodies back on their posts: kill any survivor + re-arm its ambient latch so
# ambient/placements re-places the body when the player next walks within 40.
kill @e[type=easy_npc:humanoid,tag=dc_floor_1_body]
scoreboard players set #amb_deepcore_trainer_1 ci_ambient 0
kill @e[type=easy_npc:humanoid,tag=dc_floor_2_body]
scoreboard players set #amb_deepcore_trainer_2 ci_ambient 0
kill @e[type=easy_npc:humanoid,tag=dc_floor_3_body]
scoreboard players set #amb_deepcore_trainer_3 ci_ambient 0
kill @e[type=easy_npc:humanoid,tag=dc_floor_4_body]
scoreboard players set #amb_deepcore_trainer_4 ci_ambient 0
kill @e[type=easy_npc:humanoid,tag=dc_jr_body]
scoreboard players set #amb_deepcore_jr_apprentice ci_ambient 0
kill @e[type=easy_npc:humanoid,tag=dc_pit_lead]
scoreboard players set #amb_deepcore_apprentice ci_ambient 0

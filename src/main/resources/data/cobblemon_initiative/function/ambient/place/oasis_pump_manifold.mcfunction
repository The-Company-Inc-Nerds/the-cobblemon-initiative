# oasis_pump_manifold — latch FIRST; reset #amb_oasis_pump_manifold to 0 (+ kill the body) to respawn.
scoreboard players set #amb_oasis_pump_manifold ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/oasis_pump_manifold.npc.snbt 1740.5 116 4190.5

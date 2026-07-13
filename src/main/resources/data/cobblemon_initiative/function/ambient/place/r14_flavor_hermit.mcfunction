# r14_flavor_hermit — latch FIRST; reset #amb_r14_flavor_hermit to 0 (+ kill the body) to respawn.
scoreboard players set #amb_r14_flavor_hermit ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/r14_flavor_hermit.npc.snbt 3049.5 64 2480.5

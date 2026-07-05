# watch_lantern — latch FIRST; reset #amb_watch_lantern to 0 (+ kill the body) to respawn.
scoreboard players set #amb_watch_lantern ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/watch_lantern.npc.snbt 1550.5 88 2470.5

# frontier_brain_port — latch FIRST; reset #amb_frontier_brain_port to 0 (+ kill the body) to respawn.
scoreboard players set #amb_frontier_brain_port ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/frontier_brain_port.npc.snbt 3788.5 159 2996.5

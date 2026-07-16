# frontier_caretaker — latch FIRST; reset #amb_frontier_caretaker to 0 (+ kill the body) to respawn.
scoreboard players set #amb_frontier_caretaker ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/frontier_caretaker.npc.snbt 3806.5 159 2999.5

# agent_yield_second — latch FIRST; reset #amb_agent_yield_second to 0 (+ kill the body) to respawn.
scoreboard players set #amb_agent_yield_second ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/agent_yield_second.npc.snbt 2014.5 169 2464.5

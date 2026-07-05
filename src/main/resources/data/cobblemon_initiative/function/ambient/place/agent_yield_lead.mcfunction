# agent_yield_lead — latch FIRST; reset #amb_agent_yield_lead to 0 (+ kill the body) to respawn.
scoreboard players set #amb_agent_yield_lead ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/agent_yield_lead.npc.snbt 2010.5 169 2465.5

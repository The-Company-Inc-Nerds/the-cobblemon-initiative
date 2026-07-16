# agent_pump_foreman — latch FIRST; reset #amb_agent_pump_foreman to 0 (+ kill the body) to respawn.
scoreboard players set #amb_agent_pump_foreman ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/agent_pump_foreman.npc.snbt 1728.5 66 4268.5

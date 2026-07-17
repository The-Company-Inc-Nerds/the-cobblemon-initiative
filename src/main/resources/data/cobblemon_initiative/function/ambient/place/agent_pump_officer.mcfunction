# agent_pump_officer — latch FIRST; reset #amb_agent_pump_officer to 0 (+ kill the body) to respawn.
scoreboard players set #amb_agent_pump_officer ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/agent_pump_officer.npc.snbt 1742.5 114 4192.5

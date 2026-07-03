# Roadside Work Orders — objective setup. NEEDS #minecraft:load registration
# (reported to the orchestrator via loadFunctions; function tag JSON is not edited here).
# Kill-count criteria for the NIGHT SHIFT contract + a scratch counter for item turn-ins.
scoreboard objectives add ci_kill_zombie minecraft.killed:minecraft.zombie
scoreboard objectives add ci_kill_skeleton minecraft.killed:minecraft.skeleton
scoreboard objectives add ci_kill_spider minecraft.killed:minecraft.spider
scoreboard objectives add ci_kill_total dummy
scoreboard objectives add ci_wo_count dummy

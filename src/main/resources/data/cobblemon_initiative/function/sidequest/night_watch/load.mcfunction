# First Night Watch — objectives + bossbar setup. NEEDS #minecraft:load registration
# (reported via loadFunctions; function tag JSON is not edited here).
# Idempotent + relog-safe, following the derby/sprint #init latch pattern.
# Kill counters are player-kill criteria (spec): summed into nw_total by run_tick.
scoreboard objectives add nw_z minecraft.killed:minecraft.zombie
scoreboard objectives add nw_k minecraft.killed:minecraft.skeleton
scoreboard objectives add nw_p minecraft.killed:minecraft.spider
scoreboard objectives add nw_c minecraft.killed:minecraft.creeper
scoreboard objectives add nw_total dummy
scoreboard objectives add nw_grace dummy
scoreboard objectives add ci_watch dummy
execute unless score #init ci_watch matches 1 run bossbar add cobblemon_initiative:night_watch [{"text":"✦ First Night Watch — hold until first light","color":"red"}]
scoreboard players set #init ci_watch 1
bossbar set cobblemon_initiative:night_watch color red
bossbar set cobblemon_initiative:night_watch style progress
bossbar set cobblemon_initiative:night_watch visible false
# Mid-watch relog: run_tick re-shows the bar and re-attaches the player next tick,
# so hiding here never strands a live watch.

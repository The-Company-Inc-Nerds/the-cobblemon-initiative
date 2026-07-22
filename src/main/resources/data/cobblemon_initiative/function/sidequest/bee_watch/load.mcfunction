# Bee-Swarm Wave Defense — objectives setup. NEEDS #minecraft:load registration
# (bee_watch/load is added to data/minecraft/tags/function/load.json).
# Idempotent + relog-safe, following the night_watch #init latch pattern.
# bw_kills is a player-kill criterion (bee); run_tick reads killed-since-baseline
# against the wave size so prior bee kills never auto-pass the wave.
scoreboard objectives add bw_kills minecraft.killed:minecraft.bee
scoreboard objectives add bw_spawned dummy
scoreboard objectives add bw_active dummy
scoreboard objectives add bw_baseline dummy
scoreboard objectives add ci_beewatch dummy
execute unless score #init ci_beewatch matches 1 run bossbar add cobblemon_initiative:bee_watch [{"text":"✦ Swarm on the stall — cull every bee","color":"yellow"}]
scoreboard players set #init ci_beewatch 1
bossbar set cobblemon_initiative:bee_watch color yellow
bossbar set cobblemon_initiative:bee_watch style progress
bossbar set cobblemon_initiative:bee_watch visible false
# Mid-wave relog: run_tick re-shows the bar and re-attaches the player next tick,
# so hiding here never strands a live wave.

# Bee-Swarm Wave Defense — one tick of an active wave. Run as/at the defending player.
# KILLS-SINCE-BASELINE: current bee-kill count minus the baseline snapshot from start, so only
# bees culled during THIS wave count toward clearing it (prior bee kills never auto-pass).
scoreboard players operation #done ci_beewatch = @s bw_kills
scoreboard players operation #done ci_beewatch -= @s bw_baseline
# WIN: every bee in the wave is down.
execute if score #done ci_beewatch >= @s bw_spawned run function cobblemon_initiative:sidequest/bee_watch/win
execute if score #done ci_beewatch >= @s bw_spawned run return 0
# Bossbar = bees remaining (wave size minus kills-since-baseline). Re-show + re-attach every tick
# (idempotent) so a mid-wave relog self-heals without load-order tricks.
scoreboard players operation #rem ci_beewatch = @s bw_spawned
scoreboard players operation #rem ci_beewatch -= #done ci_beewatch
execute store result bossbar cobblemon_initiative:bee_watch value run scoreboard players get #rem ci_beewatch
bossbar set cobblemon_initiative:bee_watch players @s
bossbar set cobblemon_initiative:bee_watch visible true

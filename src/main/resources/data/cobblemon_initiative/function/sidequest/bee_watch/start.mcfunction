# Bee-Swarm Wave Defense — release the swarm. Run AS THE PLAYER (dialog button, as_player).
# The as_player summon MUST live inside this called function — an as_player command may not be
# execute-rooted, so the ring of summons cannot ride inline on the dialog button.
# Guard: never re-arm a wave already running on this player.
execute if entity @s[tag=bw_active] run return 0
# BASELINE snapshot: record how many bees this player has already killed so run_tick counts only
# kills made DURING this wave (prior bee kills must never auto-pass the defense).
scoreboard players operation @s bw_baseline = @s bw_kills
# WAVE SIZE — named constant. Start with 5, we'll see.
scoreboard players set @s bw_spawned 5
tag @s add bw_active
bossbar set cobblemon_initiative:bee_watch max 5
bossbar set cobblemon_initiative:bee_watch value 5
bossbar set cobblemon_initiative:bee_watch players @s
bossbar set cobblemon_initiative:bee_watch visible true
title @s title [{"text":"THE SWARM DESCENDS","color":"yellow","bold":true}]
title @s subtitle [{"text":"Cull every bee off Mei's stall","color":"gray"}]
playsound minecraft:entity.bee.loop_aggressive master @s ~ ~ ~ 1 1
# Ring the player (standing at Mei's stall) with world-axis offsets and +1 y so the bees settle
# rather than suffocate. Bees are MobCategory.CREATURE, so /summon works inside the Takehara safe
# zone. AngerTime + AngryAt the player makes them attack immediately. Tag bw_mob for win cleanup.
execute positioned ~3 ~1 ~3 run summon minecraft:bee ~ ~ ~ {Tags:["bw_mob"],AngerTime:1200}
execute positioned ~-3 ~1 ~-2 run summon minecraft:bee ~ ~ ~ {Tags:["bw_mob"],AngerTime:1200}
execute positioned ~-3 ~1 ~3 run summon minecraft:bee ~ ~ ~ {Tags:["bw_mob"],AngerTime:1200}
execute positioned ~3 ~1 ~-3 run summon minecraft:bee ~ ~ ~ {Tags:["bw_mob"],AngerTime:1200}
execute positioned ~1 ~2 ~-1 run summon minecraft:bee ~ ~ ~ {Tags:["bw_mob"],AngerTime:1200}
# Point every fresh bee at the watcher (this player) so the wave is a live threat from tick one:
# copy the player's UUID into each bee's AngryAt. Runs at @s so distance selects the ring bees.
execute at @s as @e[tag=bw_mob,distance=..12] run data modify entity @s AngryAt set from entity @p[distance=..12] UUID

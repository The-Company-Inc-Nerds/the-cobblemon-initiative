# First Night Watch spawn wave — a small mixed hostile pulse on the Firstfurrow field. Run as/at
# the watcher (from run_tick, every 40 ticks). Self-capped: bail if the field already holds a
# full complement of watch-mobs, so pulses never pile up into an unfair swarm. Spawned mobs are
# tagged nw_mob (for the cap + win/fail cleanup); they are vanilla hostiles, so the existing
# nw_z/k/p/c cull counters still score kills toward the 8+ bonus.
scoreboard players set #nwmob ci_watch 0
execute as @e[tag=nw_mob,distance=..80] run scoreboard players add #nwmob ci_watch 1
execute if score #nwmob ci_watch matches 14.. run return 0
# Ring the watcher with world-axis offsets (so look-pitch never buries them) and +1 y so they
# settle rather than suffocate. A rolled 4th mob varies the wave.
execute positioned ~9 ~1 ~9 run summon minecraft:zombie ~ ~ ~ {Tags:["nw_mob"]}
execute positioned ~-9 ~1 ~-7 run summon minecraft:skeleton ~ ~ ~ {Tags:["nw_mob"]}
execute positioned ~-8 ~1 ~10 run summon minecraft:spider ~ ~ ~ {Tags:["nw_mob"]}
execute store result score #nwroll cd_calc run random value 1..3
execute if score #nwroll cd_calc matches 1 positioned ~11 ~1 ~-6 run summon minecraft:creeper ~ ~ ~ {Tags:["nw_mob"]}
execute if score #nwroll cd_calc matches 2 positioned ~7 ~1 ~-11 run summon minecraft:zombie ~ ~ ~ {Tags:["nw_mob"]}
execute if score #nwroll cd_calc matches 3 positioned ~-11 ~1 ~5 run summon minecraft:skeleton ~ ~ ~ {Tags:["nw_mob"]}

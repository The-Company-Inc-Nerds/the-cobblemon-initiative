# Tenants of Record — escort tick (as/at the walking player). Arrival LAST so the
# escort lines never run post-settle.
# Catch-up beyond native follow teleport: heals path-fail stands and post-relog stalls.
tp @e[tag=deng_camp,distance=48..] @s
# Re-arm follow every 100 ticks: same-Id replace re-registers the goal with a fresh
# player instance (a relog leaves the old goal holding a dead reference).
scoreboard players add #deng_walk ci_ambient 1
execute if score #deng_walk ci_ambient matches 100.. run function cobblemon_initiative:sidequest/tenants_of_record/rearm
# Arrival: the farm gate.
execute if entity @s[x=1579.5,y=88,z=2461.5,distance=..10] run function cobblemon_initiative:sidequest/tenants_of_record/homecoming

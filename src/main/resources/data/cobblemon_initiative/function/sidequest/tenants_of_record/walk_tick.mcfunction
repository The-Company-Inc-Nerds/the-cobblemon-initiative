# Tenants of Record — escort tick (as/at the walking player). Arrival LAST so the
# escort lines never run post-settle.
# Catch-up beyond native follow teleport: heals path-fail stands and post-relog stalls.
tp @e[tag=deng_camp,distance=48..] @s
# Re-arm follow every 100 ticks: same-Id replace re-registers the goal with a fresh
# player instance (a relog leaves the old goal holding a dead reference).
scoreboard players add #deng_walk ci_ambient 1
execute if score #deng_walk ci_ambient matches 100.. run function cobblemon_initiative:sidequest/tenants_of_record/rearm
# Arrival: the moment the player crosses into the Firstfurrow farm envelope (polygon bbox
# x 1547-1615, z 2459-2495), not a single 10-block gate point — miss that point and the follow
# used to re-arm forever. homecoming removes the follow + clears homecoming_walking, so completing
# on any crossing also stops the family the instant the quest completes.
execute if entity @s[x=1547,dx=68,y=70,dy=50,z=2459,dz=36] run function cobblemon_initiative:sidequest/tenants_of_record/homecoming

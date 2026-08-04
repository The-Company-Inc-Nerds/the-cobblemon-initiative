# Registered in #minecraft:tick. Every 30t while a site is active, a dust-shimmer column stands
# over the buried cache (force particles carry ~512 blocks to searching players; a no-op while
# the site chunk is unloaded). The shine is the findability device — waypoints cannot follow a
# random roll, so the desert itself marks the spot once you are close.
scoreboard players add #cache_t ci_cache 1
execute if score #cache_t ci_cache matches ..29 run return 0
scoreboard players set #cache_t ci_cache 0
execute if score #cache_site ci_cache matches 1.. run function cobblemon_initiative:sidequest/reach_cache/shine with storage cobblemon_initiative:cache active

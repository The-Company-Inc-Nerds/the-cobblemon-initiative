# Ask Ossa to re-mark the cache (lost token / unfindable site): retire the current site and
# reset — the player accepts again for a fresh roll. No refund, no penalty.
execute if score #cache_site ci_cache matches 0 run return 0
function cobblemon_initiative:sidequest/reach_cache/clean with storage cobblemon_initiative:cache active
scoreboard players set #cache_site ci_cache 0
tag @s remove reach_cache_active
tellraw @s [{"text":"Ossa wipes the bearing off her board. The sand can keep that one - ask again and she will mark another.","color":"gray"}]

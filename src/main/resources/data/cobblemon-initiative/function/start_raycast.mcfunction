# Get range from nearest NPC, or use default if not set
execute store result score #raycast raycast_steps run scoreboard players get @e[tag=sight_npc,limit=1,sort=nearest] sight_range

# If NPC has no range set (score of 0), use default
execute if score #raycast raycast_steps matches ..0 run scoreboard players operation #raycast raycast_steps = #default sight_range

function cobblemon-initiative:raycast


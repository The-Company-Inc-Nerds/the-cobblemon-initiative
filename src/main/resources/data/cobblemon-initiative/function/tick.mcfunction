# Timer logic
scoreboard players add #global raycast_timer 1
execute if score #global raycast_timer matches 5.. run scoreboard players set #global raycast_timer 0

# Reset each NPC's score
execute if score #global raycast_timer matches 0 as @e[tag=sight_npc] run scoreboard players set @s can_see_player 0

# --- TRACKING NPCs (rotate to face player) ---
execute if score #global raycast_timer matches 0 as @e[tag=sight_npc,tag=sight_track_player] at @s run summon marker ~ ~1.5 ~ {Tags:["raycast_origin","raycast_tracking"]}
execute if score #global raycast_timer matches 0 as @e[tag=raycast_tracking] at @s run tp @s ~ ~ ~ facing entity @p eyes

# --- STATIONARY NPCs (use NPC's current facing) ---
execute if score #global raycast_timer matches 0 as @e[tag=sight_npc,tag=!sight_track_player] at @s rotated as @s run summon marker ~ ~1.5 ~ {Tags:["raycast_origin","raycast_stationary"]}

# Run raycasts
execute if score #global raycast_timer matches 0 as @e[tag=raycast_origin] at @s rotated as @s positioned ^ ^ ^0.5 run function cobblemon-initiative:start_raycast

# Cleanup markers
execute if score #global raycast_timer matches 0 run kill @e[tag=raycast_origin]

# --- COMMAND MODE: Open dialog if NPC can see player and player is close ---
execute if score #global raycast_timer matches 0 as @e[tag=sight_npc,tag=sight_run_command] at @s if entity @p[distance=..3] if score @s can_see_player matches 1 run easy_npc dialog open @s @p interact

# --- NO ACTION MODE: Just let Easy NPC handle it via scoreboard conditions ---
# (NPCs without sight_run_command tag just update their can_see_player score)


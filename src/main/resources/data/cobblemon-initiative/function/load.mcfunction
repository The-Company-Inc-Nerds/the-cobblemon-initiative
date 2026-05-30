# Core scoreboards
scoreboard objectives add can_see_player dummy
scoreboard objectives add raycast_steps dummy
scoreboard objectives add raycast_timer dummy
scoreboard objectives add debug_particles dummy

# Per-NPC config scoreboards
scoreboard objectives add sight_range dummy

# Set defaults
scoreboard players set #default sight_range 100

tellraw @a {"text":"[NPC Sight] Datapack loaded!","color":"green"}


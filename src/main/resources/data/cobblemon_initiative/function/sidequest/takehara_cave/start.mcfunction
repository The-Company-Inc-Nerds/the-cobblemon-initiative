# Note 14: log the "gather a Glow Berry + Spore Blossom" mission when the challenger tells
# Cicada they'll go find them (called from gym_leader_takehara mc_locked log_button).
# Idempotent; the tracked line clears when mc_gym1_done is set (advancement grants it).
execute if entity @s[tag=takehara_cave_started] run return 0
tag @s add takehara_cave_started
tellraw @s [{"text":"Mission logged: ","color":"aqua"},{"text":"gather a Glow Berry and a Spore Blossom from the falls cave (mouth 2125 136 2703).","color":"gray"}]

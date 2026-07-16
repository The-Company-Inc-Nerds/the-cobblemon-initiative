# The Iron Ladder - claim the purse (Deepcore SQ2). Run as the player (dialog button).
# Guarded: only pays if all three rungs are down and it has not already paid. Face 700 via the
# skewed payout + an Expert Belt held item (cobblemon:expert_belt - jar-validated). One-time
# (iron_ladder_cleared). No cd_instability change - the ladder is a dojo, not a field.
execute if entity @s[tag=defeated_sq_ladder_3] unless entity @s[tag=iron_ladder_cleared] run function cobblemon_initiative:economy/payout {amount:700}
execute if entity @s[tag=defeated_sq_ladder_3] unless entity @s[tag=iron_ladder_cleared] run give @s cobblemon:expert_belt 1
execute if entity @s[tag=defeated_sq_ladder_3] unless entity @s[tag=iron_ladder_cleared] run title @s actionbar [{"text":"IRON LADDER CLEARED. ","color":"gold"},{"text":"Three rungs, no water between them. You are laddered.","color":"gray"}]
execute if entity @s[tag=defeated_sq_ladder_3] run tag @s add iron_ladder_cleared

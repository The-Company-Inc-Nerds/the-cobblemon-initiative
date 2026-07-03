# No Such Recipient — soft-fail branch, folded into the checkpoint contraband entry.
# Run as the player. Removes the Dead Letter prop and flips the quest tags so Marlow
# still closes the quest with a token reward (the cautious Nuzlocke play is never punished).
# SMOKE TEST: the component predicate must match the exact custom_name the loot table
# npc_gift/dead_letter.json writes; if serialization differs, fall back to: clear @s minecraft:paper 1
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Dead Letter"}'] 1
tag @s add letter_surrendered
tag @s remove carrying_dead_letter
tellraw @s [{"text":"The agent seals the dead letter in a case marked ","color":"gray"},{"text":"RETURNS","color":"red"},{"text":" and logs no recipient. Marlow should hear about this.","color":"gray"}]

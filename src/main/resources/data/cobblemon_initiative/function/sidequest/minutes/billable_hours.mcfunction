# Analyst wager won. Fired from the sq_hz_analyst battle on_win as the player
# (execute as @1 run ...). Banner + latch only — the 300 CD purse is the battle-block
# prize and the great balls ride the registry rewards, so this function moves NO money
# (verifier fix 5: no wager/prize double-pay).
execute unless entity @s[tag=billable_hours_done] run tellraw @s [{"text":"BILLABLE HOURS","color":"gold","bold":true},{"text":" — the Customer Confidence Challenge pays out. The stake came back doubled, and the model has a new outlier.","color":"gray"}]
execute unless entity @s[tag=billable_hours_done] run playsound minecraft:block.note_block.bell player @s ~ ~ ~ 0.8 1.2
tag @s add billable_hours_done

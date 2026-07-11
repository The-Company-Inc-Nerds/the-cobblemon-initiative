# A Frost Sentinel caught the crossing. Run as/at the seen player (gym/nifl_whiteout_run).
# Freeze tax: 3s slowness + mining fatigue (amplifier 0, particles hidden — the frost
# burst below is the tell), throttled taunt, and nifl_seen persists for THIS crossing
# (quietly cleared at the leader-side marker / re-armed at the south band). Zero damage —
# being seen only slows and shames you (hardcore fairness).
scoreboard players set @s nifl_wo 60
tag @s add nifl_seen
effect give @s minecraft:slowness 3 0 true
effect give @s minecraft:mining_fatigue 3 0 true
title @s actionbar [{"text":"The frost saw you first.","color":"aqua"}]
playsound minecraft:block.amethyst_block.chime player @s ~ ~ ~ 1 0.6
playsound minecraft:block.powder_snow.break player @s ~ ~ ~ 0.8 0.8
particle minecraft:snowflake ~ ~1 ~ 0.4 0.8 0.4 0.05 24 normal

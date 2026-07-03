# The Incomplete File stage 1 — the founding charter falls loose from the courier satchel
# when the player REFUSES to sell (the preserve fork). Called by the sq_personnel_courier
# refuse button (run as the player). One-shot; the courier gates it on not_tag doc_charter.
give @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Founding Charter — Sango"}',minecraft:lore=['{"color":"gray","italic":true,"text":"The signatory line has been sanded smooth. Under strong light, an older name almost surfaces."}']] 1
tag @s add doc_charter
title @s actionbar [{"text":"Recovered: ","color":"gold"},{"text":"the founding charter. Three of three — take them to Lucian.","color":"gray"}]

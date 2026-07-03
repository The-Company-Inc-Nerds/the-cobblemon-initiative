# The Incomplete File stage 1 — recover the sun-faded portrait backing (renamed-paper
# prop). Called by the doc_portrait_crate button (run as the player). One-shot; the crate
# gates its button on not_tag doc_portrait.
give @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Portrait Backing — sun-faded"}',minecraft:lore=['{"color":"gray","italic":true,"text":"A rectangle of unfaded paint, shaped like a face that used to hang here."}']] 1
tag @s add doc_portrait
title @s actionbar [{"text":"Recovered: ","color":"gold"},{"text":"the portrait backing. One of three misfiled records.","color":"gray"}]

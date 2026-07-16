# SQ3 The Hand That Signed It - hand the player the findable half-slagged door plate.
# Run as the player (accept button, one-shot per the_hand_started). ZERO block-edit: instead
# of a placed item entity in the slag heap, give a named plate stand-in directly - a
# minecraft:heavy_core (distinct vanilla item, so the count-check cannot collide with SQ2s
# iron ingots). Renamed to read as the recovered plate. Represents the dig; the turn-in below
# counts it back. If the showrunner prefers a literal item-entity drop at the slag-heap coord,
# swap this give for a summon item - the turn-in only cares that the count reaches 1.
give @s minecraft:heavy_core[minecraft:custom_name='{"color":"gold","italic":false,"text":"Half-Slagged Door Plate"}',minecraft:lore=['{"color":"gray","italic":true,"text":"A Company branch-office plate. The countersignature is still legible in the steel."}']] 1
title @s actionbar [{"text":"Recovered a plate. ","color":"gold"},{"text":"Bring it to Old Marren at the hearth.","color":"gray"}]

# Read the founding plaque (Caretaker Anselm's read button). One-shot lore; sets
# frontier_plaque_read (the plaque quest line clears on it). RULE 7: the plaque is the one
# un-scrubbed signature in the world, but it is described WITHOUT naming the founder — the
# name is reserved for the mirror. The rendered string carries no name and no apostrophe, so
# it stays safe if ever migrated to {do:announce}.
tag @s add frontier_plaque_read
tellraw @s [{"text":"A brass plaque, green at the corners. ","italic":true,"color":"gray"},{"text":"FOUNDING DEDICATION.","italic":true,"color":"dark_gray"},{"text":" The signature at the bottom is worn but unbroken — the only one in the region no hand ever painted over.","italic":true,"color":"gray"}]

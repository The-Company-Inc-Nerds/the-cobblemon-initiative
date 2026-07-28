# Pair shown (glow + shadow — one dual-type counts, see show_pair). Payout: 10 lapis
# from her oil-press jar — pigment coin, kept off the ledger like everything she does.
# Plain give is the established idiom for flavor errands (Bryn precedent: no CD, no
# register slot, no loot table needed for stackable vanilla items).
tag @s add faelara_pair_done
give @s minecraft:lapis_lazuli 10
tellraw @s [{"text":"Faelara looks from one to the other and back, and something in her settles. ","color":"gray"},{"text":"\"The glow and the shadow, walking in the same party. That is the whole marsh in two heartbeats — most trainers only ever carry the half they like.\"","color":"light_purple"}]
tellraw @s [{"text":"\"Here — lapis, from the press jar. It grinds into the blue the wisps burn. Hex would pocket it himself if his hands were not full of mud.\"","color":"gray","italic":true}]

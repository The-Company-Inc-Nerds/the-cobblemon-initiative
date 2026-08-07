# Karim's fossil trade — GRANT half (called by fossil_trade after a fossil was consumed).
# Run AS the player. Hands over a Trapinch (cobblemon:trapinch, jar-verified) — a living
# desert creature, NOT the fossil's own revival — and latches karim_traded so the trade is
# one-shot. Level 40 sits under the Kalahar entry cap (50) with headroom to raise; givemon
# works in dialog-button context (starters + revived-fossil gifts prove it).
tag @s add karim_traded
cobblemon-initiative givemon trapinch level=40
tellraw @s [{"text":"A Trapinch, out of a fossil? ","color":"gold"},{"text":"Karim turns the stone over, reads the maker's-mark, and sets it on his shelf like it settled a debt. Then he whistles low at the sand and something antlion-shaped churns up out of it and squints at you. \"Not from the stone,\" he says. \"From what the stone remembers. It reads the marks like I do. Take it — it will dig where you point.\"","color":"gray"}]
playsound cobblemon:pc.on master @s ~ ~ ~ 0.8 1.2
execute at @s run particle minecraft:falling_dust minecraft:sand ~ ~1 ~ 0.4 0.5 0.4 0.02 30

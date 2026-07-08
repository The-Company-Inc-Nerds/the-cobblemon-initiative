# INVENTORY contract (fork B) reward: female Combee lv 8 + heal ball. Run as the player.
clear @s minecraft:honey_bottle 3
tag @s add work_fetch_done
give @s cobblemon:heal_ball 1
# givemon = the mod's Cobblemon-API give (PokemonProperties.parse + party.add), resolving
# the player from the command source. Replaced givepokemonother (2026-07-07): it works in
# dialog buttons but a raw @s call inside a function failed "no pokemon was specified", so
# all gifts were unified here. The gender property key is honored by parse().
cobblemon-initiative givemon combee level=8 gender=female
playsound minecraft:entity.player.levelup master @s ~ ~ ~ 1 1.2
tellraw @s [{"text":"Sumi trades three bottles for one passenger. ","color":"gray"},{"text":"Received a female Combee (lv 8) and a Heal Ball.","color":"gold"}]
tellraw @s [{"text":"She will bloom into something the first gym would recognise — around level 21, once the cap lifts.","color":"dark_gray","italic":true}]

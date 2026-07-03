# INVENTORY contract (fork B) reward: female Combee lv 8 + heal ball. Run as the player.
clear @s minecraft:honey_bottle 3
tag @s add work_fetch_done
give @s cobblemon:heal_ball 1
# UNVERIFIED on Cobblemon 1.7.3: the givepokemonother command AND the gender property key.
# SMOKE-TEST before shipping. Fallback (sanctioned catch, net-neutral): replace the line
# below with a spawnpokemon at Sumi + a free ball, e.g.
#   execute at @s run spawnpokemon combee level=8 gender=female
#   give @s cobblemon:poke_ball 1
givepokemonother @s combee level=8 gender=female
playsound minecraft:entity.player.levelup master @s ~ ~ ~ 1 1.2
tellraw @s [{"text":"Sumi trades three bottles for one passenger. ","color":"gray"},{"text":"Received a female Combee (lv 8) and a Heal Ball.","color":"gold"}]
tellraw @s [{"text":"She will bloom into something the first gym would recognise — around level 21, once the cap lifts.","color":"dark_gray","italic":true}]

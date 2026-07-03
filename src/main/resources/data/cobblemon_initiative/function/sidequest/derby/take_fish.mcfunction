# Sango Classic — take exactly five fish, cod first, salmon for the remainder.
# ci_fish_cod was counted in turnin before any clearing; enumerated cases 0..5.
execute if score @s ci_fish_cod matches 5.. run clear @s minecraft:cod 5
execute if score @s ci_fish_cod matches 4 run clear @s minecraft:cod 4
execute if score @s ci_fish_cod matches 4 run clear @s minecraft:salmon 1
execute if score @s ci_fish_cod matches 3 run clear @s minecraft:cod 3
execute if score @s ci_fish_cod matches 3 run clear @s minecraft:salmon 2
execute if score @s ci_fish_cod matches 2 run clear @s minecraft:cod 2
execute if score @s ci_fish_cod matches 2 run clear @s minecraft:salmon 3
execute if score @s ci_fish_cod matches 1 run clear @s minecraft:cod 1
execute if score @s ci_fish_cod matches 1 run clear @s minecraft:salmon 4
execute if score @s ci_fish_cod matches 0 run clear @s minecraft:salmon 5

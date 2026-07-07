# Sango Classic — take exactly three fish, species priority cod → salmon → pufferfish
# → tropical fish. Per species: take = min(count, remaining), clear that literal count
# (counts were stored in turnin before any mutation), remaining -= take. `clear` needs a
# literal maxCount, so each species enumerates takes 1..3.
scoreboard players set @s ci_fish_rem 3
scoreboard players operation @s ci_fish_take = @s ci_fish_cod
execute if score @s ci_fish_take > @s ci_fish_rem run scoreboard players operation @s ci_fish_take = @s ci_fish_rem
execute if score @s ci_fish_take matches 3 run clear @s minecraft:cod 3
execute if score @s ci_fish_take matches 2 run clear @s minecraft:cod 2
execute if score @s ci_fish_take matches 1 run clear @s minecraft:cod 1
scoreboard players operation @s ci_fish_rem -= @s ci_fish_take
scoreboard players operation @s ci_fish_take = @s ci_fish_salmon
execute if score @s ci_fish_take > @s ci_fish_rem run scoreboard players operation @s ci_fish_take = @s ci_fish_rem
execute if score @s ci_fish_take matches 3 run clear @s minecraft:salmon 3
execute if score @s ci_fish_take matches 2 run clear @s minecraft:salmon 2
execute if score @s ci_fish_take matches 1 run clear @s minecraft:salmon 1
scoreboard players operation @s ci_fish_rem -= @s ci_fish_take
scoreboard players operation @s ci_fish_take = @s ci_fish_puffer
execute if score @s ci_fish_take > @s ci_fish_rem run scoreboard players operation @s ci_fish_take = @s ci_fish_rem
execute if score @s ci_fish_take matches 3 run clear @s minecraft:pufferfish 3
execute if score @s ci_fish_take matches 2 run clear @s minecraft:pufferfish 2
execute if score @s ci_fish_take matches 1 run clear @s minecraft:pufferfish 1
scoreboard players operation @s ci_fish_rem -= @s ci_fish_take
scoreboard players operation @s ci_fish_take = @s ci_fish_tropical
execute if score @s ci_fish_take > @s ci_fish_rem run scoreboard players operation @s ci_fish_take = @s ci_fish_rem
execute if score @s ci_fish_take matches 3 run clear @s minecraft:tropical_fish 3
execute if score @s ci_fish_take matches 2 run clear @s minecraft:tropical_fish 2
execute if score @s ci_fish_take matches 1 run clear @s minecraft:tropical_fish 1
scoreboard players operation @s ci_fish_rem -= @s ci_fish_take

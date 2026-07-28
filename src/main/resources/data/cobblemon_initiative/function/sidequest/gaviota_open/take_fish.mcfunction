# Gaviota Open (alpha.26 audit rulings) — take exactly ten proofs, species priority
# cod (finneon) → salmon (magikarp) → pufferfish (qwilfish) → ink (tentacool). Sango
# derby/take_fish clone at 10. Per species: take = min(count, remaining), clear that
# literal count (counts were stored in turnin before any mutation), remaining -= take.
# `clear` needs a literal maxCount, so each species enumerates takes 10..1.
scoreboard players set @s ci_open_rem 10
scoreboard players operation @s ci_open_take = @s ci_open_cod
execute if score @s ci_open_take > @s ci_open_rem run scoreboard players operation @s ci_open_take = @s ci_open_rem
execute if score @s ci_open_take matches 10 run clear @s minecraft:cod 10
execute if score @s ci_open_take matches 9 run clear @s minecraft:cod 9
execute if score @s ci_open_take matches 8 run clear @s minecraft:cod 8
execute if score @s ci_open_take matches 7 run clear @s minecraft:cod 7
execute if score @s ci_open_take matches 6 run clear @s minecraft:cod 6
execute if score @s ci_open_take matches 5 run clear @s minecraft:cod 5
execute if score @s ci_open_take matches 4 run clear @s minecraft:cod 4
execute if score @s ci_open_take matches 3 run clear @s minecraft:cod 3
execute if score @s ci_open_take matches 2 run clear @s minecraft:cod 2
execute if score @s ci_open_take matches 1 run clear @s minecraft:cod 1
scoreboard players operation @s ci_open_rem -= @s ci_open_take
scoreboard players operation @s ci_open_take = @s ci_open_salmon
execute if score @s ci_open_take > @s ci_open_rem run scoreboard players operation @s ci_open_take = @s ci_open_rem
execute if score @s ci_open_take matches 10 run clear @s minecraft:salmon 10
execute if score @s ci_open_take matches 9 run clear @s minecraft:salmon 9
execute if score @s ci_open_take matches 8 run clear @s minecraft:salmon 8
execute if score @s ci_open_take matches 7 run clear @s minecraft:salmon 7
execute if score @s ci_open_take matches 6 run clear @s minecraft:salmon 6
execute if score @s ci_open_take matches 5 run clear @s minecraft:salmon 5
execute if score @s ci_open_take matches 4 run clear @s minecraft:salmon 4
execute if score @s ci_open_take matches 3 run clear @s minecraft:salmon 3
execute if score @s ci_open_take matches 2 run clear @s minecraft:salmon 2
execute if score @s ci_open_take matches 1 run clear @s minecraft:salmon 1
scoreboard players operation @s ci_open_rem -= @s ci_open_take
scoreboard players operation @s ci_open_take = @s ci_open_puffer
execute if score @s ci_open_take > @s ci_open_rem run scoreboard players operation @s ci_open_take = @s ci_open_rem
execute if score @s ci_open_take matches 10 run clear @s minecraft:pufferfish 10
execute if score @s ci_open_take matches 9 run clear @s minecraft:pufferfish 9
execute if score @s ci_open_take matches 8 run clear @s minecraft:pufferfish 8
execute if score @s ci_open_take matches 7 run clear @s minecraft:pufferfish 7
execute if score @s ci_open_take matches 6 run clear @s minecraft:pufferfish 6
execute if score @s ci_open_take matches 5 run clear @s minecraft:pufferfish 5
execute if score @s ci_open_take matches 4 run clear @s minecraft:pufferfish 4
execute if score @s ci_open_take matches 3 run clear @s minecraft:pufferfish 3
execute if score @s ci_open_take matches 2 run clear @s minecraft:pufferfish 2
execute if score @s ci_open_take matches 1 run clear @s minecraft:pufferfish 1
scoreboard players operation @s ci_open_rem -= @s ci_open_take
scoreboard players operation @s ci_open_take = @s ci_open_ink
execute if score @s ci_open_take > @s ci_open_rem run scoreboard players operation @s ci_open_take = @s ci_open_rem
execute if score @s ci_open_take matches 10 run clear @s minecraft:ink_sac 10
execute if score @s ci_open_take matches 9 run clear @s minecraft:ink_sac 9
execute if score @s ci_open_take matches 8 run clear @s minecraft:ink_sac 8
execute if score @s ci_open_take matches 7 run clear @s minecraft:ink_sac 7
execute if score @s ci_open_take matches 6 run clear @s minecraft:ink_sac 6
execute if score @s ci_open_take matches 5 run clear @s minecraft:ink_sac 5
execute if score @s ci_open_take matches 4 run clear @s minecraft:ink_sac 4
execute if score @s ci_open_take matches 3 run clear @s minecraft:ink_sac 3
execute if score @s ci_open_take matches 2 run clear @s minecraft:ink_sac 2
execute if score @s ci_open_take matches 1 run clear @s minecraft:ink_sac 1
scoreboard players operation @s ci_open_rem -= @s ci_open_take

# The Lane door-2 gift (Kele), rerolled to EEVEE with a BOOSTED shiny chance — 1 in 20
# (natural Cobblemon shiny is ~1/8192). Run as the player from sq_kele_lane. Kele's old free
# Magikarp is now purchase-only at Deka's stand. TUNE the odds by changing the 1..20 range.
execute store result score #kele_roll ci_item run random value 1..20
execute if score #kele_roll ci_item matches 1 run cobblemon-initiative givemon eevee level=5 shiny=true
execute if score #kele_roll ci_item matches 1 run tellraw @s [{"text":"Kele presses a ","color":"gold"},{"text":"✦ SHINY ✦","color":"aqua","bold":true},{"text":" Eevee into your care — the water kept its best secret for you.","color":"gold"}]
execute unless score #kele_roll ci_item matches 1 run cobblemon-initiative givemon eevee level=5
execute unless score #kele_roll ci_item matches 1 run tellraw @s [{"text":"Kele presses a lv 5 Eevee into your care — a hundred futures in one small fox. Choose its shape yourself.","color":"gold"}]
tag @s add kele_gift
tag @s add delivered_2

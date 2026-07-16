# §2 currency interleave — Obtain a diamond. One-shot.
execute if entity @s[tag=flavor_mine_diamond_done] run return 0
tag @s add flavor_mine_diamond_done
tellraw @s [{"text":"Diamonds. The honest backing — ","color":"dark_red"},{"text":"before the Company decided wheat was easier to control than to dig for.","color":"gray","italic":true}]

# §2 currency interleave — Obtain a nether star. One-shot.
execute if entity @s[tag=flavor_nether_star_done] run return 0
tag @s add flavor_nether_star_done
tellraw @s [{"text":"A nether star. The Company would pay a fortune for it — it is their fortune. ","color":"dark_red"},{"text":"Careful who sees you holding one.","color":"gray","italic":true}]

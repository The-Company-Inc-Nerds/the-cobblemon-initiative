# §2 currency interleave — Enter the Nether. One-shot.
execute if entity @s[tag=flavor_entered_nether_done] run return 0
tag @s add flavor_entered_nether_done
tellraw @s [{"text":"Nether stars back every CobbleDollar in your pocket. ","color":"dark_red"},{"text":"This is where the Company's money is really minted — and why they guard it.","color":"gray","italic":true}]

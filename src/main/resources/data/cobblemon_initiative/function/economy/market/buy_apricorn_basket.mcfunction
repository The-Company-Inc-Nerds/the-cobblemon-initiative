# East Market Street — Bo Huan, the apricorn cart: the mixed basket, one of each color (350 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:350}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:black_apricorn 1
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:blue_apricorn 1
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:green_apricorn 1
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:pink_apricorn 1
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:red_apricorn 1
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:white_apricorn 1
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:yellow_apricorn 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased the mixed basket, one of each color. ","color":"green"},{"text":"-350 CD","color":"gray"}]

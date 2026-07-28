# Mystic Marsh east boardwalk — Elowen Mistbloom, the apothecary stall: Potion (300 CD).
# Item id jar-verified vs Cobblemon 1.7.3 (models/item/potion.json). Price matches the
# CobbleDollars default-shop canon (config/cobbledollars/default_shop.json: potion 300).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:300}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:potion 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Potion. ","color":"green"},{"text":"-300 CD","color":"gray"}]

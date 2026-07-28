# Mystic Marsh east boardwalk — Elowen Mistbloom, the apothecary stall: Awakening (200 CD).
# Item id jar-verified vs Cobblemon 1.7.3 (models/item/awakening.json). NOT stocked by the
# CobbleDollars default shop — stall exclusive, priced in line with her antidote.
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:200}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:awakening 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Awakening. ","color":"green"},{"text":"-200 CD","color":"gray"}]

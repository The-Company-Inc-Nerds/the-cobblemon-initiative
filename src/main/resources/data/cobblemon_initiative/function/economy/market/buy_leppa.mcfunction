# East Market Street — Auntie Song, the berry stand: Leppa Berry (180 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:180}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:leppa_berry 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Leppa Berry. ","color":"green"},{"text":"-180 CD","color":"gray"}]

# East Market Street — Auntie Song, the berry stand: Aspear Berry x2 (90 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:90}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:aspear_berry 2
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Aspear Berry x2. ","color":"green"},{"text":"-90 CD","color":"gray"}]

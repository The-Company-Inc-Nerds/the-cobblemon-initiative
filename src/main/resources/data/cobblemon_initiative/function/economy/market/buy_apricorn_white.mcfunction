# East Market Street — Bo Huan, the apricorn cart: White Apricorns x2 (120 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:120}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:white_apricorn 2
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased White Apricorns x2. ","color":"green"},{"text":"-120 CD","color":"gray"}]

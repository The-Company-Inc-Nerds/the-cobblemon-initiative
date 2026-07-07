# East Market Street — Madam Qiu, the mint apothecary: Modest Mint (2200 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:2200}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:modest_mint 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Modest Mint. ","color":"green"},{"text":"-2200 CD","color":"gray"}]

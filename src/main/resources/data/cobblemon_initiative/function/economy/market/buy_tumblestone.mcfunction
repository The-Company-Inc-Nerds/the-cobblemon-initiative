# East Market Street — Bo Huan, the apricorn cart: Tumblestones x4 (200 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:200}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:tumblestone 4
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Tumblestones x4. ","color":"green"},{"text":"-200 CD","color":"gray"}]

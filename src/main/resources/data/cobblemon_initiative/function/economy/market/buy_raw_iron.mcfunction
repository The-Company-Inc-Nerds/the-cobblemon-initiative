# Mirek Coalstain — Deepcore Coal Landing ore stall: Raw Iron x4 (40 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; the grant MUST stay gated on
# #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:40}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:raw_iron 4
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Bought Raw Iron x4. ","color":"green"},{"text":"-40 CD","color":"gray"}]

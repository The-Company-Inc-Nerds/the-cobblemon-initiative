# Deepcore Dust Ward signal cabinet — Helka Dustmantle: Redstone Dust x8 (100 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:100}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:redstone 8
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Redstone Dust x8. ","color":"green"},{"text":"-100 CD","color":"gray"}]

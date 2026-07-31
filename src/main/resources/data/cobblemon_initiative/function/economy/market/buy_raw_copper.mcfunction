# Mirek Coalstain — Deepcore Coal Landing ore stall: Raw Copper x4 (24 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser). Grant gated on #market_ok.
function cobblemon_initiative:economy/market/charge {price:24}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:raw_copper 4
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Bought Raw Copper x4. ","color":"green"},{"text":"-24 CD","color":"gray"}]

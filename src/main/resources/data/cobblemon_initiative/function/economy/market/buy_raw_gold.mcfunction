# Mirek Coalstain — Deepcore Coal Landing ore stall: Raw Gold x4 (64 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser). Grant gated on #market_ok.
function cobblemon_initiative:economy/market/charge {price:64}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:raw_gold 4
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Bought Raw Gold x4. ","color":"green"},{"text":"-64 CD","color":"gray"}]

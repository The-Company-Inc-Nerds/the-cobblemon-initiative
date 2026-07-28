# Mystic Marsh north reeds — Thistrel Fogroot, the brewing-supplies shelf (a21 wave).
# Gunpowder (40 CD) — turns a bottle into a splash; he calls it sneeze powder.
# Run AS THE PLAYER from a dialog buy button.
# Every grant line MUST stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:40}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:gunpowder 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Gunpowder. ","color":"green"},{"text":"-40 CD","color":"gray"}]

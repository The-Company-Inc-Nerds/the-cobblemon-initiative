# Mystic Marsh north reeds — Thistrel Fogroot, the brewing-supplies shelf (a21 wave).
# Redstone Dust (40 CD) — stretches a brew longer. Run AS THE PLAYER from a dialog
# buy button.
# Every grant line MUST stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:40}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:redstone 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Redstone Dust. ","color":"green"},{"text":"-40 CD","color":"gray"}]

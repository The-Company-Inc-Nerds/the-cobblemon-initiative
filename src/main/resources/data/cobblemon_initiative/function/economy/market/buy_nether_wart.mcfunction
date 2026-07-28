# Mystic Marsh north reeds — Thistrel Fogroot, the brewing-supplies shelf (a21 wave).
# Nether Wart (60 CD) — the base of every awkward brew. Run AS THE PLAYER from a
# dialog buy button.
# Every grant line MUST stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:60}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:nether_wart 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Nether Wart. ","color":"green"},{"text":"-60 CD","color":"gray"}]

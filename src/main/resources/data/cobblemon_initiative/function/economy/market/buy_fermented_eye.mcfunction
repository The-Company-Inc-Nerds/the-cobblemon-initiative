# Mystic Marsh north reeds — Thistrel Fogroot, the brewing-supplies shelf (a21 wave).
# Fermented Spider Eye (90 CD) — the corrupting agent; jarred, labeled, absolutely
# not for eating. Run AS THE PLAYER from a dialog buy button.
# Every grant line MUST stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:90}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:fermented_spider_eye 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Fermented Spider Eye. ","color":"green"},{"text":"-90 CD","color":"gray"}]

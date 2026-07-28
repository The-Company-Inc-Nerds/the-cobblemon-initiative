# Mystic Marsh north reeds — Thistrel Fogroot, the brewing-supplies shelf (a21 wave:
# raw STILL INGREDIENTS, not finished brews — Elowen keeps the bottled shelf; the
# split is deliberate). Blaze Rod (300 CD) — the fire under every cauldron.
# Run AS THE PLAYER from a dialog buy button.
# Every grant line MUST stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:300}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:blaze_rod 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Blaze Rod. ","color":"green"},{"text":"-300 CD","color":"gray"}]

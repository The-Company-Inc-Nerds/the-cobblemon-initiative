# Mystic Marsh north reeds — Thistrel Fogroot, the brewing-supplies shelf (a21 wave).
# Glowstone Dust (80 CD) — strengthens a brew; he swears his is wisp-shed, not mined.
# Run AS THE PLAYER from a dialog buy button.
# Every grant line MUST stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:80}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:glowstone_dust 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Glowstone Dust. ","color":"green"},{"text":"-80 CD","color":"gray"}]

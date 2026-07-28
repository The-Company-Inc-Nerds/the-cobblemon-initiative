# Mystic Marsh SE boardwalk — Veyric Ashenmark (the other one), the mushroom crate: Brown Mushroom x4 (60 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:60}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:brown_mushroom 4
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Brown Mushroom x4. ","color":"green"},{"text":"-60 CD","color":"gray"}]

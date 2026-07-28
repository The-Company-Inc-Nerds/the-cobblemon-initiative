# Mystic Marsh SE boardwalk — Veyric Ashenmark (the other one), the mushroom crate: Mushroom Stew (90 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:90}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:mushroom_stew 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Mushroom Stew. ","color":"green"},{"text":"-90 CD","color":"gray"}]

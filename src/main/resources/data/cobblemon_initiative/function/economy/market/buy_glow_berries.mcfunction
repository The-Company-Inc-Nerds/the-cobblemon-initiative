# Mystic Marsh SE boardwalk — Veyric Ashenmark (the other one), the mushroom crate: Glow Berries x3 (120 CD).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:120}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:glow_berries 3
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Glow Berries x3. ","color":"green"},{"text":"-120 CD","color":"gray"}]

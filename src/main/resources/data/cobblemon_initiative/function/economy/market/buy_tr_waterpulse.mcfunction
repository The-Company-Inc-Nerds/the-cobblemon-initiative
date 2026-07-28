# Mystic Marsh machine counter — Ilyana Mossveil (badge-2 thread rack): TR Water Pulse (1200 CD).
# Item id jar-verified vs SimpleTMs-fabric-2.3.3 (assets/simpletms/models/item/tr_waterpulse.json).
# Stock deliberately DISJOINT from the Takehara counter (Mika) — marsh/fairy band.
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:1200}
execute if score #market_ok cd_calc matches 1.. run give @s simpletms:tr_waterpulse 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased TR Water Pulse. ","color":"green"},{"text":"-1200 CD","color":"gray"}]

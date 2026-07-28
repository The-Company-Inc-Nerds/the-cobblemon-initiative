# Mystic Marsh machine counter — Ilyana Mossveil (badge-2 thread rack): TR Draining Kiss (1000 CD).
# Item id jar-verified vs SimpleTMs-fabric-2.3.3 (assets/simpletms/models/item/tr_drainingkiss.json).
# Stock deliberately DISJOINT from the Takehara counter (Mika) — marsh/fairy band.
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:1000}
execute if score #market_ok cd_calc matches 1.. run give @s simpletms:tr_drainingkiss 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased TR Draining Kiss. ","color":"green"},{"text":"-1000 CD","color":"gray"}]

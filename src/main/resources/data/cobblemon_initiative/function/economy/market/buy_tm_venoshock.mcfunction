# Mystic Marsh machine counter — Ilyana Mossveil (badge-3 loom cabinet): TM Venoshock (2400 CD).
# Item id jar-verified vs SimpleTMs-fabric-2.3.3 (assets/simpletms/models/item/tm_venoshock.json).
# TMs are finite (8 uses, unrepairable) per config/simpletms/main.json — same as the Mika counter.
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:2400}
execute if score #market_ok cd_calc matches 1.. run give @s simpletms:tm_venoshock 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased TM Venoshock. ","color":"green"},{"text":"-2400 CD","color":"gray"}]

# Takehara Falls machine counter — Machine Counter Mika (badge-2 TM page): TM Facade (3600 CD).
# Item id jar-verified vs SimpleTMs-fabric-2.3.3 (movelearnitems/default.json + tm_/tr_ registry prefix).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:3600}
execute if score #market_ok cd_calc matches 1.. run give @s simpletms:tm_facade 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased TM Facade. ","color":"green"},{"text":"-3600 CD","color":"gray"}]

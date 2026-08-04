# Kalahar Poke-Mart machine shelf — Martkeeper Hassan Qadir (badge-6 desert TM page, a18 playtest
# N20): TM Drill Run (4500 CD). Item id jar-verified vs SimpleTMs-fabric-2.3.3 (tm_ registry prefix,
# lowercase move name, no separators).
# Run AS THE PLAYER from the tm_shelf buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:4500}
execute if score #market_ok cd_calc matches 1.. run give @s simpletms:tm_drillrun 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased TM Drill Run. ","color":"green"},{"text":"-4500 CD","color":"gray"}]

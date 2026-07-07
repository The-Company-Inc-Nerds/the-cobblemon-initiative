# Sango Town back lane — Torn-Label Tadashi, the TR peddler: TR Will-O-Wisp (1500 CD).
# Item id jar-verified vs SimpleTMs-fabric-2.3.3 (movelearnitems/default.json + tm_/tr_ registry prefix).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:1500}
execute if score #market_ok cd_calc matches 1.. run give @s simpletms:tr_willowisp 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased TR Will-O-Wisp. ","color":"green"},{"text":"-1500 CD","color":"gray"}]

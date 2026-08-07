# Kalahar Reach — Yasmin Aset, the dune-nomad's survival kit: Heal Powder (450 CD).
# Item id jar-verified vs Cobblemon 1.7.3 (models/item/heal_powder.json). A bitter herbal
# cure the nomads carry — full status heal, cheaper than a bought Full Heal because the
# Pokemon likes it less. Fixed price. Run AS THE PLAYER from a dialog buy button
# (ExecAsUser; bare `function` is allowlisted). Grant gated on #market_ok.
function cobblemon_initiative:economy/market/charge {price:450}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:heal_powder 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Heal Powder. ","color":"green"},{"text":"-450 CD","color":"gray"}]

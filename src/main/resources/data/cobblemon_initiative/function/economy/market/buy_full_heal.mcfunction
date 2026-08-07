# Kalahar Reach — Yasmin Aset, the dune-nomad's survival kit: Full Heal (600 CD).
# Item id jar-verified vs Cobblemon 1.7.3 (models/item/full_heal.json). Cures every status
# — the nomad's "reset" for a bad crossing (sand-scald, sleep, poison). Priced between the
# single-status cures (250) and a super potion (700). Fixed price. Run AS THE PLAYER from a
# dialog buy button (ExecAsUser; bare `function` is allowlisted). Grant gated on #market_ok.
function cobblemon_initiative:economy/market/charge {price:600}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:full_heal 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Full Heal. ","color":"green"},{"text":"-600 CD","color":"gray"}]

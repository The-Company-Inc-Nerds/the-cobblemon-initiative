# Kalahar Reach — Yasmin Aset, the dune-nomad's survival kit: Hyper Potion (1200 CD).
# Item id jar-verified vs Cobblemon 1.7.3 (models/item/hyper_potion.json). Priced one tier
# above the Super Potion canon (potion 300 / super 700 / hyper 1200) — the desert-crossing
# heal. Fixed price. Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function`
# is allowlisted). The grant MUST stay gated on #market_ok.
function cobblemon_initiative:economy/market/charge {price:1200}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:hyper_potion 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Hyper Potion. ","color":"green"},{"text":"-1200 CD","color":"gray"}]

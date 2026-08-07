# Kalahar Reach — Yasmin Aset, the dune-nomad's survival kit: Rare Candy (4000 CD).
# Item id jar-verified vs Cobblemon 1.7.3 (models/item/rare_candy.json). Priced PREMIUM
# on purpose: a rare candy is a full level in a level-capped run, so it must stay a
# rare splurge, never a grind loop (hardcore no-farm rule). Fixed price (cd_instability
# is the only price driver; this stall does not jitter). Run AS THE PLAYER from a dialog
# buy button (ExecAsUser; bare `function` is allowlisted). The shared charge macro probes
# affordability and deducts; the grant MUST stay gated on #market_ok.
function cobblemon_initiative:economy/market/charge {price:4000}
execute if score #market_ok cd_calc matches 1.. run give @s cobblemon:rare_candy 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Rare Candy. ","color":"green"},{"text":"-4000 CD","color":"gray"}]

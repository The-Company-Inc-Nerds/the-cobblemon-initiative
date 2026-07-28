# Mystic Marsh east boardwalk — Elowen Mistbloom, the apothecary stall (alpha.2 re-stock:
# TRAINER-side vanilla brews, playtest ruling "not pokemon potion to sell, but should sell
# minecraft potions" — Pokemon medicine stays the mart's shelf). Potion of Healing II (400 CD).
# Component JSON lives HERE, never in the dialog cmd string (dialog cmds may not carry
# quotes — economy/market/charge rule). Run AS THE PLAYER from a dialog buy button.
# Every grant line MUST stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:400}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:potion[potion_contents={potion:"minecraft:strong_healing"}] 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Potion of Healing II. ","color":"green"},{"text":"-400 CD","color":"gray"}]

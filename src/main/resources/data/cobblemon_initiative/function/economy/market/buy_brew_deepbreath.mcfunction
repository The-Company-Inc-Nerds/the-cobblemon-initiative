# Elowen Mistbloom, apothecary stall (alpha.2 trainer-brew re-stock): Potion of Water
# Breathing 8:00 (350 CD) — quietly load-bearing for the drowned-stair shrine descent.
# See buy_brew_healing.mcfunction for the pattern notes.
function cobblemon_initiative:economy/market/charge {price:350}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:potion[potion_contents={potion:"minecraft:long_water_breathing"}] 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Potion of Water Breathing. ","color":"green"},{"text":"-350 CD","color":"gray"}]

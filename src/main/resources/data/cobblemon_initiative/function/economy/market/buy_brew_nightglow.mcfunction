# Elowen Mistbloom, apothecary stall (alpha.2 trainer-brew re-stock): Potion of Night
# Vision 8:00 (250 CD) — the marsh-lamp rule made this the house special. See
# buy_brew_healing.mcfunction for the pattern notes.
function cobblemon_initiative:economy/market/charge {price:250}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:potion[potion_contents={potion:"minecraft:long_night_vision"}] 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Potion of Night Vision. ","color":"green"},{"text":"-250 CD","color":"gray"}]

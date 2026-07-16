# Beacon-in-stock affordability check (run AS the player from phone/tick). HomesteadManager
# publishes the player's next-beacon price to ci_flavor #beacon_price. Pre-seed the macro arg to
# MAX_INT so a missing/unset price reads as unaffordable (never a false ring), then read the live
# price and probe affordability WITHOUT charging (cobbledollars pay is a net-zero self-pay).
data modify storage cobblemon_initiative:phone price set value 2147483647
execute store result storage cobblemon_initiative:phone price int 1 run scoreboard players get #beacon_price ci_flavor
function cobblemon_initiative:phone/beacon_probe with storage cobblemon_initiative:phone

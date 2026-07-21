# Beacon affordability probe (macro; arg: price). Run AS the player. `cobbledollars pay @s N` is a
# net-zero self-pay that store-results the amount when affordable and 0 when broke — so this checks
# "can afford" without spending. If affordable, ring Liang's beacon-in-stock call (once; the caller
# guards on call_beacon_stock_done). (cd_calc declared by economy/load.)
scoreboard players set #ph_ok cd_calc 0
$execute store result score #ph_ok cd_calc run cobbledollars pay @s $(price)
execute if score #ph_ok cd_calc matches 1.. run function cobblemon_initiative:phone/ring_beacon

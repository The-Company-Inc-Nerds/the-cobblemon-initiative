# Homestead beacon purchase probe. Args: price (computed by HomesteadManager.buyBeacon — never
# call this directly). Run AS the buying player. Reuses the CobbleDollars pay-probe rail (see
# daycare/pickup_fee): `pay` is a net-zero self-pay that store-results the amount when affordable
# and 0 when broke; the follow-up `remove` is the real deduction. HomesteadManager reads #hs_ok
# back next tick and grants the beacon on success. (cd_calc is declared by economy/load.)
scoreboard players set #hs_ok cd_calc 0
$execute store result score #hs_ok cd_calc run cobbledollars pay @s $(price)
$execute if score #hs_ok cd_calc matches 1.. run cobbledollars remove @s $(price)

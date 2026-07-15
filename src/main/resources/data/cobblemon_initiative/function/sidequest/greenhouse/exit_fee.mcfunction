# Greenhouse escorted-exit fee — 150 CD, BALANCE-GATED. Was a bare `cobbledollars remove` that
# escorted a broke player out for FREE. Run AS the player. Pay-probe before removing; broke ->
# the message points them at the free "walk back down the stairs" exit instead.
scoreboard players set #exit_ok cd_calc 0
execute store result score #exit_ok cd_calc run cobbledollars pay @s 150
execute if score #exit_ok cd_calc matches 1.. run cobbledollars remove @s 150
execute if score #exit_ok cd_calc matches 1.. run title @s actionbar {"text":"Escorted out. The Company appreciates your discretion.","color":"gray"}
execute if score #exit_ok cd_calc matches 0 run title @s actionbar {"text":"The envelope is short. Walk yourself back down the stairs — same view, no charge.","color":"gray"}

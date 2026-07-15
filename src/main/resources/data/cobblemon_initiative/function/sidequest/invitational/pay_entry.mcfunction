# Waterside Invitational entry — 150 CD, BALANCE-GATED. Was a bare `cobbledollars remove` that
# let a broke player sign up (and reach the disbursed purse) for FREE. Run AS the player.
# Pay-probe: net-zero self-pay, store RESULT before removing (0 = broke, 150 = can afford).
scoreboard players set #invit_ok cd_calc 0
execute store result score #invit_ok cd_calc run cobbledollars pay @s 150
execute if score #invit_ok cd_calc matches 1.. run cobbledollars remove @s 150
execute if score #invit_ok cd_calc matches 1.. run tag @s add invit_entered
execute if score #invit_ok cd_calc matches 1.. run title @s actionbar {"text":"Entered the Waterside Invitational. Round one: Reedhand Lumo, down the bank.","color":"aqua"}
execute if score #invit_ok cd_calc matches 0 run title @s actionbar {"text":"The desk cannot take an entry it cannot bank. Come back when the fee clears.","color":"gray"}

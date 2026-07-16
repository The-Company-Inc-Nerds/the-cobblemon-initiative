# THE DOOR DOWNTOWN - pay 300 CD to skip the wager and just hear the door math. Run as the player
# from the dialog hear_button. Bespoke (NOT the auto route-decline) because it must charge AND then
# deliver the free pointer - a plain bow-out button would strand the pointer. Uses the pay-probe rail
# (the heal_paid probe pattern): CobbleDollars `pay` soft-fails when broke, so gate on `store result`
# (0 = broke, 300 = affordable). Paid -> Verified Charge receipt. The POINTER IS FREE AND ALWAYS
# DELIVERED (win, lose, or decline) - a broke player still gets the door math; only the fee is skipped.
scoreboard players set #decl_ok cd_calc 0
execute store result score #decl_ok cd_calc run cobbledollars pay @s 300
execute if score #decl_ok cd_calc matches 1.. run cobbledollars remove @s 300
execute if score #decl_ok cd_calc matches 1.. run title @s actionbar [{"text":"Verified Charge: 300 CD. ","color":"gold"},{"text":"Consultation processed. The broker keeps his edge and his manners.","color":"gray"}]
function cobblemon_initiative:sidequest/door/deliver_pointer

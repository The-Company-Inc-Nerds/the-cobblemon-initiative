# The Reach Remembers (Boundary Stones, Q1) - the no-battle fork on the guarded stone.
# Run AS the player (stone-3 cite button). Pay-probe the 150 CD filing fee using the same
# balance-gate idiom as economy/market/charge (bytecode-verified, CobbleDollars 2.0.0-Beta-5.1):
# `pay` checks the SOURCE balance before any mutation and self-pay is net-zero, so `store result`
# is the reliable signal (0 = broke, amount = paid). Reset first so a parse-denied line cannot
# leave a stale score. On success: charge, set stone3_guard_clear, announce the surveyor stands
# down. On broke: fail-soft tellraw, no tag. cd_calc is declared by economy/load (#minecraft:load).
scoreboard players set #cite_ok cd_calc 0
execute store result score #cite_ok cd_calc run cobbledollars pay @s 150
execute if score #cite_ok cd_calc matches 1.. run cobbledollars remove @s 150
execute if score #cite_ok cd_calc matches 1.. run tag @s add stone3_guard_clear
execute if score #cite_ok cd_calc matches 1.. run title @s actionbar [{"text":"Filing accepted. ","color":"gold"},{"text":"The surveyor reads his own boundary law, sighs, and steps aside. -150 CD","color":"gray"}]
execute if score #cite_ok cd_calc matches 0 run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"The clipboard does not move on credit. (150 CD required)","color":"gray"}]

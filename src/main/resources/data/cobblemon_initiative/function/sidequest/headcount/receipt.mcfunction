# Head Count — the payroll receipt. Run as the player (dialog button, as_player), right
# after the skew-aware payout. The sting: the SYSTEM cannot resolve the payee. This is
# the first written, on-screen admission that the game does not know who the player is.
# (Raw tellraw lives here, not in dialog cmd values, because dialog text forbids quotes.)
playsound minecraft:entity.villager.work_cartographer master @s ~ ~ ~ 1 1
tellraw @s [{"text":"────────────────────────────","color":"dark_gray"}]
tellraw @s [{"text":"COMPANY FIELD PAYROLL — RECEIPT","color":"gold","bold":true}]
tellraw @s [{"text":"ITEM: ","color":"gray"},{"text":"verified wild capture, blossom path census","color":"white"}]
tellraw @s [{"text":"AMOUNT: ","color":"gray"},{"text":"250 CD","color":"green"},{"text":" — APPROVED","color":"dark_green"}]
tellraw @s [{"text":"PAYEE: ","color":"gray"},{"text":"[UNVERIFIED]","color":"red","bold":true}]
tellraw @s [{"text":"────────────────────────────","color":"dark_gray"}]

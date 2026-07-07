# Head Count — the payroll receipt. Run as the player (dialog button, as_player), right
# after the skew-aware payout. The sting: the SYSTEM cannot resolve the payee. This is
# the first written, on-screen admission that the game does not know who the player is.
# (Raw tellraw lives here, not in dialog cmd values, because dialog text forbids quotes.)
playsound minecraft:entity.villager.work_cartographer master @s ~ ~ ~ 1 1
tellraw @s [{"text":"────────────────────────────","color":"dark_gray"}]
tellraw @s [{"text":"COMPANY FIELD PAYROLL — RECEIPT","color":"gold","bold":true}]
tellraw @s [{"text":"ITEM: ","color":"gray"},{"text":"verified wild capture, blossom path census","color":"white"}]
tellraw @s [{"text":"AMOUNT: ","color":"gray"},{"text":"250 CD","color":"green"},{"text":" — APPROVED","color":"dark_green"}]
# Rolled survey column (review B6 — the salvaged Ume roll): a ZERO-VALUE ledger line.
# Grain Suitability stays an unexplained column — wheat foreshadow only, never named.
execute store result score #survey cd_calc run random value 1..5
execute if score #survey cd_calc matches 1 run tellraw @s [{"text":"GRAIN SUITABILITY: ","color":"gray"},{"text":"0.41 — recorded, not explained","color":"dark_gray"}]
execute if score #survey cd_calc matches 2 run tellraw @s [{"text":"GRAIN SUITABILITY: ","color":"gray"},{"text":"0.58 — recorded, not explained","color":"dark_gray"}]
execute if score #survey cd_calc matches 3 run tellraw @s [{"text":"GRAIN SUITABILITY: ","color":"gray"},{"text":"0.63 — flagged for follow-up","color":"dark_gray"}]
execute if score #survey cd_calc matches 4 run tellraw @s [{"text":"GRAIN SUITABILITY: ","color":"gray"},{"text":"0.77 — flagged for follow-up","color":"dark_gray"}]
execute if score #survey cd_calc matches 5 run tellraw @s [{"text":"GRAIN SUITABILITY: ","color":"gray"},{"text":"0.92 — do not discuss with respondents","color":"dark_gray"}]
tellraw @s [{"text":"PAYEE: ","color":"gray"},{"text":"[UNVERIFIED]","color":"red","bold":true}]
tellraw @s [{"text":"────────────────────────────","color":"dark_gray"}]

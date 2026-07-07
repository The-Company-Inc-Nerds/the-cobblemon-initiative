# Macro (run as @s=player): the COMPANY-BRANDED payout receipt. Args: paid, rate, raw.
# Fires only where taking Company money is the dramatic point (the branded receipt is
# the tell): census sign fork, courier sell fork, Invitational purse, Adjusted Retail.
# Everything else routes through the unbranded pay_macro.
$cobbledollars give @s $(paid)
$title @s actionbar [{"text":"Company Verified Rate ","color":"gold"},{"text":"$(rate)%","color":"yellow"},{"text":"   ","color":"gray"},{"text":"$(raw)","color":"dark_gray"},{"text":" → ","color":"gray"},{"text":"$(paid) ","color":"green"},{"text":"CD","color":"dark_green"}]
# LINE-ITEM ROLL (review B6, 2026-07-06): one rolled ZERO-VALUE ledger line in the
# Company voice on every branded payout — pure flavor, never touches the paid amount
# (randomness invariants, ENGINE_FINDINGS §3). Pool B (11-14, nervous) replaces the
# roll once the instability index hits 40 — the receipts start sweating with the coin.
execute store result score #li cd_calc run random value 1..10
execute if score #idx cd_instability matches 40.. store result score #li cd_calc run random value 11..14
execute if score #li cd_calc matches 1 run tellraw @s [{"text":"ADJUSTMENT: ","color":"gray"},{"text":"rounding, in the Company's favor. (0 CD)","color":"dark_gray"}]
execute if score #li cd_calc matches 2 run tellraw @s [{"text":"LINE ITEM: ","color":"gray"},{"text":"verification surcharge — waived, this once.","color":"dark_gray"}]
execute if score #li cd_calc matches 3 run tellraw @s [{"text":"LINE ITEM: ","color":"gray"},{"text":"goodwill, amortized over ten quarters.","color":"dark_gray"}]
execute if score #li cd_calc matches 4 run tellraw @s [{"text":"PROCESSING: ","color":"gray"},{"text":"0 CD. Your patience is priced in.","color":"dark_gray"}]
execute if score #li cd_calc matches 5 run tellraw @s [{"text":"NOTE: ","color":"gray"},{"text":"this receipt is a courtesy, not an entitlement.","color":"dark_gray"}]
execute if score #li cd_calc matches 6 run tellraw @s [{"text":"AUDIT TRAIL: ","color":"gray"},{"text":"intact. It is always intact.","color":"dark_gray"}]
execute if score #li cd_calc matches 7 run tellraw @s [{"text":"LINE ITEM: ","color":"gray"},{"text":"gratitude adjustment — 0 CD, non-negotiable.","color":"dark_gray"}]
execute if score #li cd_calc matches 8 run tellraw @s [{"text":"RETENTION: ","color":"gray"},{"text":"transaction archived for training purposes.","color":"dark_gray"}]
execute if score #li cd_calc matches 9 run tellraw @s [{"text":"SURCHARGE: ","color":"gray"},{"text":"none today. The Company remembers who asked.","color":"dark_gray"}]
execute if score #li cd_calc matches 10 run tellraw @s [{"text":"LINE ITEM: ","color":"gray"},{"text":"loyalty accrual — pending review since forever.","color":"dark_gray"}]
execute if score #li cd_calc matches 11 run tellraw @s [{"text":"ADJUSTMENT: ","color":"gray"},{"text":"figures pending re-verification. Do not re-count them.","color":"dark_gray"}]
execute if score #li cd_calc matches 12 run tellraw @s [{"text":"NOTE: ","color":"gray"},{"text":"the rate is fine. The rate has always been fine.","color":"dark_gray"}]
execute if score #li cd_calc matches 13 run tellraw @s [{"text":"LINE ITEM: ","color":"gray"},{"text":"confidence restoration levy — deferred. Again.","color":"dark_gray"}]
execute if score #li cd_calc matches 14 run tellraw @s [{"text":"PLEASE RETAIN THIS RECEIPT. ","color":"gray"},{"text":"PLEASE.","color":"dark_gray"}]

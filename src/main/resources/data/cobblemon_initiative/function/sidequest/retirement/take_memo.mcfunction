# SQ4 Retirement Package - Severance hands over the memo trail and disappears east.
# Run as the player (guarded by the take_memo button: asset_recovery_clear present). Latch
# retirement_memo_taken, pay 700 via the skewed payout + a major training gift, give the memo
# lore item (a renamed minecraft:paper, jar-safe), and set the story breadcrumb
# scrubbing_artifact_memo (one-way, feeds the Board/Founder synthesis chain). The memo text
# circles the reveal (retired -> emptied -> there was never a founder), never closes it.
tag @s add retirement_memo_taken
tag @s add scrubbing_artifact_memo
function cobblemon_initiative:economy/payout {amount:700}
function cobblemon_initiative:economy/reward/major
give @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"The Retirement Memo Trail"}',minecraft:lore=['{"color":"gray","italic":true,"text":"Retired. Reassigned. There was never a founder."}','{"color":"gray","italic":true,"text":"A scrubbing job caught in the act, in dated order."}']] 1
title @s actionbar [{"text":"RETIREMENT PACKAGE SECURED. ","color":"gold"},{"text":"Nobody who wrote it believed it.","color":"gray"}]

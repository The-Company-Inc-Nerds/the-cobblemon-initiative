# The Nervous Clerk (Ryujin SQ1) - take the re-verified ledger page. Run AS the player
# (dialog take_page button). Stand-down beat, no battle. Keepsake page (vanilla paper),
# 400 CD at full face value (idx 25 -> payout skew ~ 0), a standard training gift, and the
# ryujin_ledger_taken latch. The cover-up leaks one page at a time.
give @s minecraft:paper 1
function cobblemon_initiative:economy/payout {amount:400}
function cobblemon_initiative:economy/reward/standard
tag @s add ryujin_ledger_taken
title @s actionbar [{"text":"Filed. ","color":"gold"},{"text":"The cover-up leaks one page at a time.","color":"gray"}]

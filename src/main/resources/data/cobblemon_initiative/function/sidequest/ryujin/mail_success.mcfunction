# The Broken Mail (Ryujin SQ3) - Tetsu mends two suits, keeps one, hands the player the
# other. Run as the player (guarded by turn_in_scales: count 8.. and not already done).
# Consumes 8 dragon scales, returns 1 as the mended-mail keepsake (no dedicated armor item is
# jar-validated, so the kept scale is the reward token - Genji rod / Marigold charm precedent).
# Face 500 CD via the skewed payout (full at idx 25) + a standard training gift, then latches
# ryujin_mail_done. cobblemon:dragon_scale is jar-valid (1.7.3 items.txt).
clear @s cobblemon:dragon_scale 8
give @s cobblemon:dragon_scale 1
function cobblemon_initiative:economy/payout {amount:500}
function cobblemon_initiative:economy/reward/standard
tag @s add ryujin_mail_done
title @s actionbar [{"text":"Scale-mail mended. ","color":"gold"},{"text":"A dragon sheds armor a dragon cannot freeze.","color":"gray"}]

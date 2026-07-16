# Verified Weather - the witness fee, paid deliberately SHORT (the receipt gag). Run as the
# player. economy/payout skews the received number visibly under the 60 face - the joke lands
# on the receipt. Latches mm_board_done so the micro-quest ends.
function cobblemon_initiative:economy/payout {amount:60}
tag @s add mm_board_done
title @s actionbar [{"text":"ADJUSTMENT: ","color":"gold"},{"text":"rounding, in the Company favor. Witness fee processed.","color":"gray"}]

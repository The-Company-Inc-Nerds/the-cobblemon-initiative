# The Incomplete File stage 2 turn-in — Lucian files three intact Revision Notices pulled
# clean of the surveyor. Called by the sq_personnel_file stage2_turnin button (run as the
# player). Reward: 600 CD. One-shot; gated on ci_notices >= 3 + not notices_filed.
function cobblemon_initiative:economy/payout {amount:600}
tag @s add notices_filed
title @s actionbar [{"text":"Revision notices filed. ","color":"gold"},{"text":"The record is almost whole now. Almost.","color":"gray"}]

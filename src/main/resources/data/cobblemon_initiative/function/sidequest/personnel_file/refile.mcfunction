# The Incomplete File stage 3 (post-HQ) — Lucian reassembles the record and re-files it
# under a name that still refuses to resolve. Called by the sq_personnel_file stage3 button
# (run as the player). End-game-scale back pay: 4000 CD + one exp_candy_m. One-shot; gated
# on defeated_villain_boss + notices_filed + not file_refiled.
function cobblemon_initiative:economy/payout {amount:4000}
give @s cobblemon:exp_candy_m 1
tag @s add file_refiled
title @s actionbar [{"text":"The file is closed. ","color":"gold"},{"text":"He files it under a name he declines to read aloud.","color":"gray"}]

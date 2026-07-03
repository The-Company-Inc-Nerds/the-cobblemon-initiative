# The Incomplete File stage 1 turn-in — Lucian files the three recovered records. Called
# by the sq_personnel_file stage1_turnin button (run as the player). Preserve fork reward:
# 300 CD + tactical consumables (potions are quest-only currency in this shop economy;
# the antidote is for Cicada Scolipede). One-shot; gated on the three doc tags + not docs_filed.
function cobblemon_initiative:economy/payout {amount:300}
give @s cobblemon:potion 3
give @s cobblemon:antidote 1
tag @s add docs_filed
title @s actionbar [{"text":"Stage one filed. ","color":"gold"},{"text":"Lucian cross-references the three, and goes a little quiet.","color":"gray"}]

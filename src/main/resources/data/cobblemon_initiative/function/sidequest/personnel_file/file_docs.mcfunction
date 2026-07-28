# The Incomplete File turn-in — Lucian files the three recovered records. Called
# by the sq_personnel_file stage1_turnin button (run as the player). Preserve fork reward:
# 300 CD + tactical consumables (potions are quest-only currency in this shop economy;
# the antidote is for Cicada Scolipede). One-shot; gated on the three doc tags + not docs_filed.
# alpha.26 audit rulings: stages 2-3 (Revision Notices + post-HQ grand filing) were cut
# with the never-placed ghost cast — this filing now CLOSES the quest.
function cobblemon_initiative:economy/payout {amount:300}
give @s cobblemon:potion 3
give @s cobblemon:antidote 1
# Lucian actually TAKES the three records — custom_name component predicates matching the
# exact set_name each give_doc_* writes (lore may be omitted: predicates match per-component;
# the pattern is runtime-proven with the same paper predicates in the doc props).
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Portrait Backing — sun-faded"}'] 1
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Ledger Page — re-signed"}'] 1
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Founding Charter — Sango"}'] 1
tag @s add docs_filed
title @s actionbar [{"text":"The record is filed. ","color":"gold"},{"text":"Lucian cross-references the three, and goes a little quiet.","color":"gray"}]

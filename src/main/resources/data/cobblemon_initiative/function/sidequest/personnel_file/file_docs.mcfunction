# The Incomplete File stage 1 turn-in — Lucian files the three recovered records. Called
# by the sq_personnel_file stage1_turnin button (run as the player). Preserve fork reward:
# 300 CD + tactical consumables (potions are quest-only currency in this shop economy;
# the antidote is for Cicada Scolipede). One-shot; gated on the three doc tags + not docs_filed.
function cobblemon_initiative:economy/payout {amount:300}
give @s cobblemon:potion 3
give @s cobblemon:antidote 1
# Lucian actually TAKES the three records — custom_name component predicates matching the
# exact set_name each give_doc_* writes (lore may be omitted: predicates match per-component;
# the pattern is runtime-proven in memo/surrender_letter).
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Portrait Backing — sun-faded"}'] 1
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Ledger Page — re-signed"}'] 1
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Founding Charter — Sango"}'] 1
tag @s add docs_filed
title @s actionbar [{"text":"Stage one filed. ","color":"gold"},{"text":"Lucian cross-references the three, and goes a little quiet.","color":"gray"}]

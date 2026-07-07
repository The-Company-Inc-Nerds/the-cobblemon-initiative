# The paper hub — Lucian files Field Memo 7-12 (the Sting Operation determinations:
# honey REJECTED, wheat APPROVED, proceed to Hua Zhan). Takehara finally produces
# archive traffic. Called by the sq_personnel_file memo712_turn_in button (run as the
# player). 200 CD (skew-aware) + minor pack, sibling rate to the other filings. Takes
# the paper (custom_name predicate matches sting/give_field_memo). One-shot; gated on
# sting_memo_taken + not field_memo_filed.
function cobblemon_initiative:economy/payout {amount:200}
loot give @s loot cobblemon_initiative:npc_gift/training_minor
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Field Memo 7-12 — Pilot Determinations"}'] 1
tag @s add field_memo_filed
title @s actionbar [{"text":"Field memo filed. ","color":"gold"},{"text":"Somewhere east, a granary is already counted.","color":"gray"}]

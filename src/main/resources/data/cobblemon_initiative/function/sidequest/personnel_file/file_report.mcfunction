# The paper hub — Lucian files Yield Report RZ-7 (Greenspace 7, Under-Performing).
# Called by the sq_personnel_file rezoning_filed button (run as the player). Pays like
# its sibling filings — 200 CD (skew-aware) + a minor training pack — so the archive
# ritual never stiffs the player while it is being established. Mirrors file_docs:
# Lucian actually TAKES the report (custom_name predicate matches the rezoning_memo
# loot-table set_name). One-shot; gated on yield_report_taken + not scrub_report_filed.
function cobblemon_initiative:economy/payout {amount:200}
loot give @s loot cobblemon_initiative:npc_gift/training_minor
clear @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Yield Report RZ-7"}'] 1
tag @s add scrub_report_filed
title @s actionbar [{"text":"Yield report filed. ","color":"gold"},{"text":"The records fee files itself too.","color":"gray"}]

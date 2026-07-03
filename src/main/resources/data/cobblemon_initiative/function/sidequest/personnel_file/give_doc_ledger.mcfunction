# The Incomplete File stage 1 — recover the re-signed ledger page (renamed-paper prop).
# Called by the doc_ledger_barrel button (run as the player). One-shot; the barrel gates
# its button on not_tag doc_ledger.
give @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Ledger Page — re-signed"}',minecraft:lore=['{"color":"gray","italic":true,"text":"Two signatures. The newer written straight over the older, which is not how signatures work."}']] 1
tag @s add doc_ledger
title @s actionbar [{"text":"Recovered: ","color":"gold"},{"text":"the re-signed ledger page. Two of three.","color":"gray"}]

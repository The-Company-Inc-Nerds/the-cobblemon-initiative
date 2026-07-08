# The Incomplete File stage 1 — recover the re-signed ledger page (renamed-paper prop).
# Run as the player by docprop.DocPropManager when the ledger barrel (2584 107 2925) is
# clicked; the handler only fires while file_opened is set and doc_ledger is not (one-shot).
give @s minecraft:paper[minecraft:custom_name='{"color":"gold","italic":false,"text":"Ledger Page — re-signed"}',minecraft:lore=['{"color":"gray","italic":true,"text":"Two signatures. The newer written straight over the older, which is not how signatures work."}']] 1
tag @s add doc_ledger
title @s actionbar [{"text":"Recovered: ","color":"gold"},{"text":"the re-signed ledger page. Two of three.","color":"gray"}]

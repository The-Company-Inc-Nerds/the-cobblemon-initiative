# Mystic Marsh high stilt — Thalorin Wispbrook, the enchant-glass rack: Depth Strider II book (1200 CD).
# The marsh-thematic headliner — his dialog sells it hard (flooded paths, drowned list).
# 1.21.1 COMPONENT SYNTAX jar-verified: ItemEnchantments.CODEC = withAlternative(full record,
# LEVELS_CODEC) — the bare enchantment->level map parses on 1.21.1 (see buy_ench_unbreaking3).
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:1200}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:enchanted_book[minecraft:stored_enchantments={"minecraft:depth_strider":2}] 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Depth Strider II enchant-glass. ","color":"green"},{"text":"-1200 CD","color":"gray"}]

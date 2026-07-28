# Mystic Marsh high stilt — Thalorin Wispbrook, the enchant-glass rack: Unbreaking III book (1500 CD).
# 1.21.1 COMPONENT SYNTAX jar-verified vs the loom-remapped 1.21.1 jar: ItemEnchantments.CODEC is
# withAlternative(full record, LEVELS_CODEC), so the bare enchantment->level map below parses on
# 1.21.1 (the {levels:{...}} wrapper form also works; the bare map is the form that survives 1.21.2+).
# Enchantment id = vanilla registry name. Keep this form consistent across all four buy_ench_* wrappers.
# Run AS THE PLAYER from a dialog buy button (ExecAsUser; bare `function` is allowlisted).
# The shared charge macro probes affordability and deducts; every grant line below MUST
# stay gated on #market_ok (see economy/market/charge.mcfunction).
function cobblemon_initiative:economy/market/charge {price:1500}
execute if score #market_ok cd_calc matches 1.. run give @s minecraft:enchanted_book[minecraft:stored_enchantments={"minecraft:unbreaking":3}] 1
execute if score #market_ok cd_calc matches 1.. run title @s actionbar [{"text":"Purchased Unbreaking III enchant-glass. ","color":"green"},{"text":"-1500 CD","color":"gray"}]

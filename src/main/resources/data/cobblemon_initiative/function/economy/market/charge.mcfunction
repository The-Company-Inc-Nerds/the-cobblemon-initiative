# East Market Street shared charge probe (MACRO). Args: price (numeric ONLY — dialog cmd
# strings may not carry quotes, so item ids live in the per-ware buy_* wrappers, never here).
# Balance gate copied from economy/heal_paid.mcfunction (bytecode-verified, CobbleDollars
# 2.0.0-Beta-5.1): `pay` checks the SOURCE balance before any mutation and a self-pay is
# net-zero; the fail path soft-fails (returns 0 WITHOUT throwing), so `store success` would
# read 1 either way — `store result` is the reliable signal: 0 = broke, amount = paid.
# Reset first so a parse-denied line cannot leave a stale score. `remove` clamps at 0
# (fail-soft), so every wrapper MUST gate its give on #market_ok matches 1.. — an ungated
# give is free stock for broke players. cd_calc is declared by economy/load.mcfunction
# (#minecraft:load) — no extra load wiring needed.
scoreboard players set #market_ok cd_calc 0
$execute store result score #market_ok cd_calc run cobbledollars pay @s $(price)
$execute if score #market_ok cd_calc matches 1.. run cobbledollars remove @s $(price)
$execute if score #market_ok cd_calc matches 0 run title @s actionbar [{"text":"Payment declined. ","color":"red"},{"text":"The stall keeps no tabs. ($(price) CD required)","color":"gray"}]

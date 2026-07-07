# Dawn driver (review B6, 2026-07-06) — registered in #minecraft:tick. Two jobs:
#
# 1) DAY LATCH (the sprint/tick pattern): when `time query day` moves past the stored
#    value, run dawn_roll (Company Morning Memo + the East Market Street ware roll).
#    First-ever load: #last_day is unset, the `unless =` comparison fails, dawn_roll
#    fires once — the market gets stocked and announced on install, then daily.
execute store result score #day ci_dawn run time query day
execute unless score #day ci_dawn = #last_day ci_dawn run function cobblemon_initiative:economy/dawn_roll
scoreboard players operation #last_day ci_dawn = #day ci_dawn
#
# 2) MIRROR the market day onto players (the price_check #idx mirror pattern): dialog
#    band tags read @s, the runtime keeps the roll on the #ware_day fake player. The
#    compiler-generated hz_market_eq_1/2 tags gate the trader ware buttons off this.
execute if score #ware_day hz_market matches 1.. run scoreboard players operation @a hz_market = #ware_day hz_market

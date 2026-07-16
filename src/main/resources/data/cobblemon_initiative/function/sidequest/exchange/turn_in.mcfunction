# EXCHANGE RATE - turn-in count-check (Genji idiom, prop-tag flavor). Run as the player from the
# teller turnin button. Only pays if all three reserve-tag props are re-verified and not already paid;
# otherwise an actionbar nudge. Everything real happens in reward, gated on not-yet-done.
execute if entity @s[tag=ci_reserve_1,tag=ci_reserve_2,tag=ci_reserve_3] unless entity @s[tag=ci_reserves_done] run function cobblemon_initiative:sidequest/exchange/reward
execute unless entity @s[tag=ci_reserve_1] run title @s actionbar [{"text":"The teller waits: ","color":"gray"},{"text":"three reserve tags, downtown. Not all re-verified yet.","color":"yellow"}]
execute if entity @s[tag=ci_reserve_1] unless entity @s[tag=ci_reserve_2] run title @s actionbar [{"text":"The teller waits: ","color":"gray"},{"text":"three reserve tags, downtown. Not all re-verified yet.","color":"yellow"}]
execute if entity @s[tag=ci_reserve_2] unless entity @s[tag=ci_reserve_3] run title @s actionbar [{"text":"The teller waits: ","color":"gray"},{"text":"three reserve tags, downtown. Not all re-verified yet.","color":"yellow"}]

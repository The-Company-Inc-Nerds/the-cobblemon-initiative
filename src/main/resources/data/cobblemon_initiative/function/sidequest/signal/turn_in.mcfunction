# SIGNAL INTEGRITY - turn-in count-check (Genji idiom, prop-tag flavor). Run as the player from
# Rell turnin button. Only pays if all three boards are scrubbed and not already paid; otherwise an
# actionbar nudge. Everything real happens in reward, gated on not-yet-done.
execute if entity @s[tag=ci_board_1,tag=ci_board_2,tag=ci_board_3] unless entity @s[tag=ci_signal_done] run function cobblemon_initiative:sidequest/signal/reward
execute unless entity @s[tag=ci_board_1] run title @s actionbar [{"text":"Rell shakes his head: ","color":"gray"},{"text":"three boards, downtown. Not all scrubbed yet.","color":"yellow"}]
execute if entity @s[tag=ci_board_1] unless entity @s[tag=ci_board_2] run title @s actionbar [{"text":"Rell shakes his head: ","color":"gray"},{"text":"three boards, downtown. Not all scrubbed yet.","color":"yellow"}]
execute if entity @s[tag=ci_board_2] unless entity @s[tag=ci_board_3] run title @s actionbar [{"text":"Rell shakes his head: ","color":"gray"},{"text":"three boards, downtown. Not all scrubbed yet.","color":"yellow"}]

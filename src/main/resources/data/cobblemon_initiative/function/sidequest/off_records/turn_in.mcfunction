# OFF THE RECORDS - turn-in count-check (Genji idiom, prop-tag flavor). Run as the player from
# Maren turnin button. Only pays if all three pages are recovered and not already filed; otherwise an
# actionbar nudge. Everything real happens in reward, gated on not-yet-done.
execute if entity @s[tag=ci_page_1,tag=ci_page_2,tag=ci_page_3] unless entity @s[tag=ci_file_done] run function cobblemon_initiative:sidequest/off_records/reward
execute unless entity @s[tag=ci_page_1] run title @s actionbar [{"text":"Maren counts the pages: ","color":"gray"},{"text":"three drops in the annex. Not all recovered yet.","color":"yellow"}]
execute if entity @s[tag=ci_page_1] unless entity @s[tag=ci_page_2] run title @s actionbar [{"text":"Maren counts the pages: ","color":"gray"},{"text":"three drops in the annex. Not all recovered yet.","color":"yellow"}]
execute if entity @s[tag=ci_page_2] unless entity @s[tag=ci_page_3] run title @s actionbar [{"text":"Maren counts the pages: ","color":"gray"},{"text":"three drops in the annex. Not all recovered yet.","color":"yellow"}]

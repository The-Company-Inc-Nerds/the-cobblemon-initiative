# Run as/at a player within 6 blocks of the Branch Manager lectern who lacks
# hz_minutes_heard. Clone of sidequest/memo/near_tent.mcfunction: mirrors
# can_see_player as the stream-legible EYES ON YOU / CLEAR actionbar meter and runs
# the 8-second (160-tick) unseen loiter countdown on ci_loiter_hz. At 160 held ticks
# the minutes are heard (hear_minutes fires directly — no arming tag needed here, the
# hz_minutes_heard latch on the caller makes it one-time).

# Seen by any office staff: reset the countdown and flash the warning.
execute if entity @e[tag=hz_office_staff,scores={can_see_player=1}] run scoreboard players reset @s ci_loiter_hz
execute if entity @e[tag=hz_office_staff,scores={can_see_player=1}] run title @s actionbar [{"text":"EYES ON YOU","color":"red","bold":true},{"text":" — the reading starts over","color":"gray"}]

# Unseen: count up.
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] run scoreboard players add @s ci_loiter_hz 1

# Visible countdown, 8 -> 1 seconds, in 20-tick bands.
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] if score @s ci_loiter_hz matches 1..20 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 8","color":"gray"}]
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] if score @s ci_loiter_hz matches 21..40 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 7","color":"gray"}]
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] if score @s ci_loiter_hz matches 41..60 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 6","color":"gray"}]
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] if score @s ci_loiter_hz matches 61..80 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 5","color":"gray"}]
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] if score @s ci_loiter_hz matches 81..100 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 4","color":"gray"}]
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] if score @s ci_loiter_hz matches 101..120 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 3","color":"gray"}]
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] if score @s ci_loiter_hz matches 121..140 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 2","color":"gray"}]
execute unless entity @e[tag=hz_office_staff,scores={can_see_player=1}] if score @s ci_loiter_hz matches 141..159 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 1","color":"gray"}]

# 8 seconds held: the district reads itself out.
execute if score @s ci_loiter_hz matches 160.. run function cobblemon_initiative:sidequest/minutes/hear_minutes

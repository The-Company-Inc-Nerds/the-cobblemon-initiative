# Run as/at a player within 14 blocks of a checkpoint agent who lacks memo_heard.
# Mirrors can_see_player as a stream-legible EYES ON YOU / CLEAR actionbar meter and
# runs the 8-second (160-tick) unseen loiter countdown that arms the eavesdrop trigger.

# Seen by either agent: reset the countdown and flash the warning.
execute if entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] run scoreboard players reset @s ci_loiter
execute if entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] run title @s actionbar [{"text":"EYES ON YOU","color":"red","bold":true}]

# Unseen: count up (only while the eavesdrop is not yet armed).
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] run scoreboard players add @s ci_loiter 1

# Visible countdown, 8 -> 1 seconds, in 20-tick bands.
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] if score @s ci_loiter matches 1..20 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 8","color":"gray"}]
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] if score @s ci_loiter matches 21..40 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 7","color":"gray"}]
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] if score @s ci_loiter matches 41..60 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 6","color":"gray"}]
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] if score @s ci_loiter matches 61..80 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 5","color":"gray"}]
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] if score @s ci_loiter matches 81..100 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 4","color":"gray"}]
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] if score @s ci_loiter matches 101..120 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 3","color":"gray"}]
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] if score @s ci_loiter matches 121..140 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 2","color":"gray"}]
execute if entity @s[tag=!memo_loiter] unless entity @e[tag=checkpoint_agent,distance=..14,scores={can_see_player=1}] if score @s ci_loiter matches 141..159 run title @s actionbar [{"text":"CLEAR","color":"green","bold":true},{"text":" — hold position 1","color":"gray"}]

# 8 seconds held: arm the eavesdrop (the ON_DISTANCE_NEAR trigger on the field agent
# opens the overheard_memo chain while memo_loiter is set and memo_heard is not).
execute if entity @s[tag=!memo_loiter] if score @s ci_loiter matches 160.. run function cobblemon_initiative:sidequest/memo/loiter_ready

# Armed prompt.
execute if entity @s[tag=memo_loiter] run title @s actionbar [{"text":"Voices at the tent flap","color":"gold"},{"text":" — move in quietly and listen","color":"gray"}]

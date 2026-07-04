# Run as/at a player within 6 blocks of the Yield Analyst who lacks yield_report_taken.
# Mirrors can_see_player as a stream-legible ON THE RECORD / OFF THE RECORD actionbar meter
# and runs the 8-second (160-tick) unseen loiter countdown that arms the eavesdrop trigger.

# Seen by the analyst: reset the countdown and flash the warning.
execute if entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] run scoreboard players reset @s ci_audit
execute if entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] run title @s actionbar [{"text":"ON THE RECORD","color":"red","bold":true}]

# Unseen: count up (only while the eavesdrop is not yet armed).
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] run scoreboard players add @s ci_audit 1

# Visible countdown, 8 -> 1 seconds, in 20-tick bands.
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] if score @s ci_audit matches 1..20 run title @s actionbar [{"text":"OFF THE RECORD","color":"green","bold":true},{"text":" — hold position 8","color":"gray"}]
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] if score @s ci_audit matches 21..40 run title @s actionbar [{"text":"OFF THE RECORD","color":"green","bold":true},{"text":" — hold position 7","color":"gray"}]
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] if score @s ci_audit matches 41..60 run title @s actionbar [{"text":"OFF THE RECORD","color":"green","bold":true},{"text":" — hold position 6","color":"gray"}]
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] if score @s ci_audit matches 61..80 run title @s actionbar [{"text":"OFF THE RECORD","color":"green","bold":true},{"text":" — hold position 5","color":"gray"}]
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] if score @s ci_audit matches 81..100 run title @s actionbar [{"text":"OFF THE RECORD","color":"green","bold":true},{"text":" — hold position 4","color":"gray"}]
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] if score @s ci_audit matches 101..120 run title @s actionbar [{"text":"OFF THE RECORD","color":"green","bold":true},{"text":" — hold position 3","color":"gray"}]
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] if score @s ci_audit matches 121..140 run title @s actionbar [{"text":"OFF THE RECORD","color":"green","bold":true},{"text":" — hold position 2","color":"gray"}]
execute if entity @s[tag=!audit_loiter] unless entity @e[tag=yield_analyst,distance=..6,scores={can_see_player=1}] if score @s ci_audit matches 141..159 run title @s actionbar [{"text":"OFF THE RECORD","color":"green","bold":true},{"text":" — hold position 1","color":"gray"}]

# 8 seconds held: arm the eavesdrop (the ON_DISTANCE_NEAR trigger on the analyst opens
# the overheard_valuation chain while audit_loiter is set and yield_report_taken is not).
execute if entity @s[tag=!audit_loiter] if score @s ci_audit matches 160.. run function cobblemon_initiative:sidequest/audit/loiter_ready

# Armed prompt.
execute if entity @s[tag=audit_loiter] run title @s actionbar [{"text":"He is reading the draft aloud","color":"gold"},{"text":" — move in quietly and listen","color":"gray"}]

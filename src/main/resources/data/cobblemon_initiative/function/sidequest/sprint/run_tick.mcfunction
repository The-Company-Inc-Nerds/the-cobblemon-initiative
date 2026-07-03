# Quarterly Sprint — one tick of an active run. Run as/at the sprinting player.
scoreboard players remove @s ci_sprint 1
execute store result bossbar cobblemon_initiative:sprint value run scoreboard players get @s ci_sprint
# Warning pings at 60 / 30 / 10 / 5 / 3 / 2 / 1 seconds (rising pitch for the countdown).
execute if score @s ci_sprint matches 1200 run title @s actionbar [{"text":"60 seconds","color":"yellow"}]
execute if score @s ci_sprint matches 1200 run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 0.8
execute if score @s ci_sprint matches 600 run title @s actionbar [{"text":"30 seconds","color":"yellow"}]
execute if score @s ci_sprint matches 600 run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 0.9
execute if score @s ci_sprint matches 200 run title @s actionbar [{"text":"10 seconds","color":"gold"}]
execute if score @s ci_sprint matches 200 run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1
execute if score @s ci_sprint matches 100 run title @s actionbar [{"text":"5","color":"red"}]
execute if score @s ci_sprint matches 100 run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.2
execute if score @s ci_sprint matches 60 run title @s actionbar [{"text":"3","color":"red"}]
execute if score @s ci_sprint matches 60 run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.4
execute if score @s ci_sprint matches 40 run title @s actionbar [{"text":"2","color":"red"}]
execute if score @s ci_sprint matches 40 run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.6
execute if score @s ci_sprint matches 20 run title @s actionbar [{"text":"1","color":"red"}]
execute if score @s ci_sprint matches 20 run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.8
# Finish zone: the delivery bell at the Takehara arch (shared vertex 1923,2584).
execute if entity @s[x=1918,dx=12,y=-64,dy=384,z=2578,dz=12] run function cobblemon_initiative:sidequest/sprint/finish
# Time expired (finish removes ci_sprinting, so the tag guard prevents a double fire).
execute if entity @s[tag=ci_sprinting] if score @s ci_sprint matches ..0 run function cobblemon_initiative:sidequest/sprint/fail

# Liberation ceremony — <FIELD> — LIBERATED, run as/at the player (called by
# free_field_apply with storage cobblemon_initiative:liberation {display, n}).
# Wheat-gold title, bell, fireworks over the barn. The FIRST liberation gets the
# quieter, heavier subtitle — the moment the war turns personal; every later one
# advances the arc scoreboard out loud (n of 6). 10 fields exist (farm_1..farm_10); the goal is
# ANY 6, so n>=6 gets its own broken-monopoly beat and 7..10 keep celebrating without overflowing /6.
title @s times 10 70 20
$execute if score @s fields_liberated matches 2..5 run title @s subtitle [{"text":"The commodity loses ground — $(n) of 6","color":"gray"}]
execute if score @s fields_liberated matches 6 run title @s subtitle [{"text":"Six fields freed — the monopoly breaks.","color":"#C9A227"}]
$execute if score @s fields_liberated matches 7.. run title @s subtitle [{"text":"$(n) fields freed — nothing left for them to hold.","color":"#C9A227"}]
execute if score @s fields_liberated matches 1 run title @s subtitle [{"text":"The Company will notice.","color":"gray","italic":true}]
$title @s title [{"text":"$(display) — LIBERATED","color":"#C9A227","bold":true}]
playsound minecraft:block.bell.use master @s ~ ~ ~ 1 0.8
playsound minecraft:entity.villager.celebrate master @s ~ ~ ~ 0.8 1
# Fireworks over the freed field (wheat-gold bursts around the player — the fight ends
# at the barn, so the sky lights where the deed happened).
execute at @s run summon minecraft:firework_rocket ~3 ~1 ~2 {LifeTime:24,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"large_ball",colors:[I;13214247],has_trail:true}],flight_duration:1}}}}
execute at @s run summon minecraft:firework_rocket ~-3 ~1 ~-2 {LifeTime:30,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"small_ball",colors:[I;16766720],fade_colors:[I;13214247]}],flight_duration:1}}}}
execute at @s run summon minecraft:firework_rocket ~2 ~1 ~-4 {LifeTime:36,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"burst",colors:[I;13214247],has_twinkle:true}],flight_duration:1}}}}

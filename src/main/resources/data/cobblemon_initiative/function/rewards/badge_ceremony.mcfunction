# Two-stage badge ceremony, stage 1 — the TRIUMPH layer. Run as the player from a
# per-gym wrapper (rewards/gym/badge_N, added as a gym-config command reward row next
# to the memory/gym/frag_N row). Macro args: {name:"Falls Badge", cap:22}.
# Stage 2 is the memory fragment title, deferred +4s inside memory/grant_fragment —
# fireworks and toast first, then the purple silence lands alone.
title @s times 5 60 15
$title @s subtitle [{"text":"Level cap raised to $(cap)","color":"gray"}]
$title @s title [{"text":"$(name) — EARNED","color":"gold","bold":true}]
playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 1
playsound minecraft:entity.villager.celebrate master @s ~ ~ ~ 1 1
# Celebration volley — five gold bursts around the podium.
execute at @s run summon minecraft:firework_rocket ~2 ~1 ~1 {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"large_ball",colors:[I;16766720],has_trail:true}],flight_duration:1}}}}
execute at @s run summon minecraft:firework_rocket ~-2 ~1 ~1 {LifeTime:26,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"small_ball",colors:[I;16777215],fade_colors:[I;16766720]}],flight_duration:1}}}}
execute at @s run summon minecraft:firework_rocket ~1 ~1 ~-2 {LifeTime:32,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"burst",colors:[I;16766720],has_twinkle:true}],flight_duration:1}}}}
execute at @s run summon minecraft:firework_rocket ~-1 ~1 ~2 {LifeTime:38,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"star",colors:[I;16766720],has_trail:true}],flight_duration:1}}}}
execute at @s run summon minecraft:firework_rocket ~ ~1 ~ {LifeTime:44,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"large_ball",colors:[I;16777215],fade_colors:[I;16766720],has_twinkle:true}],flight_duration:1}}}}

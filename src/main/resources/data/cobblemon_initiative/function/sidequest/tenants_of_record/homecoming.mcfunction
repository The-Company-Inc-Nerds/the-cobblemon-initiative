# Tenants of Record — HOMECOMING. Run as @s = the player, from Old Dengs walk_home button
# (button gate: farm_1_free + not homecoming_paid — this function is only reachable from there).
# The emotional payload of the farm-liberation backbone: liberate farm_1 and the camp walks home on camera.

# 0) Walk teardown (round 13c) — soft no-ops when the escort never started.
tag @s remove homecoming_walking
easy_npc objective @e[tag=deng_old,limit=1] remove follow player
easy_npc objective @e[tag=deng_granny,limit=1] remove follow player
easy_npc objective @e[tag=deng_haoran,limit=1] remove follow player

# 1) Latch FIRST so a double-click cannot double-pay.
tag @s add homecoming_paid

# 2) The first honest sale of the season, paid through the skewed rail — economy/payout applies the
#    instability haircut, and the haircut shorting even this sale is the point of the scene.
function cobblemon_initiative:economy/payout {amount:380}
function cobblemon_initiative:economy/reward/standard

# 3) The hamper — 8 bread, 6 oran berries, 1 potion (premium: nothing on the road sells it this early).
give @s minecraft:bread 8
give @s cobblemon:oran_berry 6
give @s cobblemon:potion 1
# Rolled side item (review B6 weighted drip): the fixed hamper is the FLOOR; one rolled
# extra rides on top, announced. Bonus-only, latched one-time — no farm loop.
execute store result score #hamper cd_calc run random value 1..4
execute if score #hamper cd_calc matches 1 run give @s cobblemon:sitrus_berry 1
execute if score #hamper cd_calc matches 1 run tellraw @s [{"text":"Granny Yun adds: ","color":"gray"},{"text":"a Sitrus Berry, saved for a good day.","color":"gold"}]
execute if score #hamper cd_calc matches 2 run give @s cobblemon:cheri_berry 2
execute if score #hamper cd_calc matches 2 run tellraw @s [{"text":"Granny Yun adds: ","color":"gray"},{"text":"two Cheri Berries off the fence row.","color":"gold"}]
execute if score #hamper cd_calc matches 3 run give @s minecraft:pumpkin_pie 2
execute if score #hamper cd_calc matches 3 run tellraw @s [{"text":"Granny Yun adds: ","color":"gray"},{"text":"two slices of pie — packed for a siege, as ever.","color":"gold"}]
execute if score #hamper cd_calc matches 4 run give @s cobblemon:pecha_berry 2
execute if score #hamper cd_calc matches 4 run tellraw @s [{"text":"Granny Yun adds: ","color":"gray"},{"text":"two Pecha Berries, sweet ones, for the road back.","color":"gold"}]

# 4) VERIFIER FIX 2: the walk home is wired, not implied. The showrunner tags the three bodies at
#    placement (deng_old / deng_granny / deng_haoran, plus deng_camp on all three); this moves them
#    through the north fence gap the Yield Officer used to hold. Old Deng stands at the farm gate.
tp @e[tag=deng_old,limit=1] 1579.5 88 2461.5
tp @e[tag=deng_granny,limit=1] 1582.5 88 2464.5
tp @e[tag=deng_haoran,limit=1] 1576.5 88 2464.5

# 5) The beat, on camera.
title @s subtitle {"text":"The family walks home.","color":"gold"}
title @s title {"text":"Firstfurrow","color":"#C9A227"}
tellraw @s {"text":"Old Deng folds the camp with two hands and no ceremony. Fifty paces, four generations, and the kettle is on the family stove before it has finished cooling.","color":"gold"}

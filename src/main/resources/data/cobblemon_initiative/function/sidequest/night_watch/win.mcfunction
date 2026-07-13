# First Night Watch — dawn broke with the watcher on post. Run as/at the player.
# One-time latch (first_watch_done): the lantern coda + a free hook for later
# granary-keeper relief flavor. Payout is the skewed mod-routed rate (spec: 500 CD,
# top of the grunt band — a full night is the hardest work on the route).
tag @s remove ci_watching
tag @s add first_watch_done
scoreboard players set @s nw_grace 0
bossbar set cobblemon_initiative:night_watch visible false
title @s title [{"text":"FIRST LIGHT","color":"gold","bold":true}]
title @s subtitle [{"text":"The field held — the family brings it in","color":"yellow"}]
playsound minecraft:block.bell.use master @s ~ ~ ~ 1 1
function cobblemon_initiative:economy/payout {amount:500}
function cobblemon_initiative:economy/reward/major
# Breakfast hamper: 6 bread, 4 oran berries, 1 potion.
give @s minecraft:bread 6
give @s cobblemon:oran_berry 4
give @s cobblemon:potion 1
# Rolled side item (review B6 weighted drip): the fixed hamper is the FLOOR; one rolled
# extra rides on top, announced. Bonus-only, one-time function — no farm loop.
execute store result score #hamper cd_calc run random value 1..4
execute if score #hamper cd_calc matches 1 run give @s cobblemon:cheri_berry 2
execute if score #hamper cd_calc matches 1 run tellraw @s [{"text":"Tucked under the cloth: ","color":"gray"},{"text":"two Cheri Berries, still dew-cold.","color":"yellow"}]
execute if score #hamper cd_calc matches 2 run give @s cobblemon:leppa_berry 1
execute if score #hamper cd_calc matches 2 run tellraw @s [{"text":"Tucked under the cloth: ","color":"gray"},{"text":"a Leppa Berry from the house jar.","color":"yellow"}]
execute if score #hamper cd_calc matches 3 run give @s minecraft:pumpkin_pie 2
execute if score #hamper cd_calc matches 3 run tellraw @s [{"text":"Tucked under the cloth: ","color":"gray"},{"text":"two slices of pumpkin pie, watch-night baking.","color":"yellow"}]
execute if score #hamper cd_calc matches 4 run give @s cobblemon:oran_berry 3
execute if score #hamper cd_calc matches 4 run tellraw @s [{"text":"Tucked under the cloth: ","color":"gray"},{"text":"three more Oran Berries — the field counts its debts.","color":"yellow"}]
tellraw @s [{"text":"Night rate paid: ","color":"gray"},{"text":"500 CD","color":"gold"},{"text":" and a breakfast hamper. Culls on the ledger: ","color":"gray"},{"score":{"name":"@s","objective":"nw_total"},"color":"red"},{"text":".","color":"gray"}]
# VERIFIER FIX (3, minor): the family arrival stays off-camera flavor — no NPC tp
# (the camp NPCs belong to Tenants of Record and have no known UUIDs at author time).
tellraw @s [{"text":"Down the road, the Deng family walks the rows in the sunrise — the first free yield comes in whole.","color":"yellow","italic":true}]
# 8+ culls bonus: the marquee heal_ball, kept per verifier option A (dedup flagged to
# the showrunner in the quest report — see verify notes).
execute if score @s nw_total matches 8.. run give @s cobblemon:heal_ball 1
execute if score @s nw_total matches 8.. run tellraw @s [{"text":"Eight or more kept off the rows — the family adds a Heal Ball. Bring them home, in both senses.","color":"light_purple"}]

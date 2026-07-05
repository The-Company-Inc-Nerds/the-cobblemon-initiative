# Off the Record debrief at Lucian, once all three errands are done. Run as the player.
# 300 CD always; the heal_ball clean-sweep bonus only if no auditor ever logged you across
# the two carries (off_record_blown never latched). Advancement downgraded to a praise line.
function cobblemon_initiative:economy/payout {amount:300}
loot give @s loot cobblemon_initiative:npc_gift/training_standard
tag @s add off_record_complete
execute unless entity @s[tag=off_record_blown] run give @s cobblemon:heal_ball 1
execute unless entity @s[tag=off_record_blown] run title @s actionbar [{"text":"OFF THE RECORD","color":"gold","bold":true},{"text":" - never once seen. Nothing on file, nothing on you.","color":"gray"}]
execute if entity @s[tag=off_record_blown] run title @s actionbar [{"text":"Filed and paid. ","color":"gold"},{"text":"They logged you along the way, but the neighbors got their errands.","color":"gray"}]

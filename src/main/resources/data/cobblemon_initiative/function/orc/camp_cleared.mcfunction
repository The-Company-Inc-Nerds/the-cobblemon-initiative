# Active orc camp CLEARED (a22) — run POSITIONED at the active pin from orc/tick when the absence
# poll finds no ci_orc mob left. Record the pin as #orc_last_pin (rerolled against next dawn), clear
# #orc_active to 0 (a fresh pin rolls at the NEXT dawn — "each day after one is cleared out"), reset
# #orc_raised. Then the shared ceremony + spoils (major for the three high ridges P2-P4, minor for
# the rest). Audience @a[distance=..64] is positioned at the pin (wide enough for a mid-fight backpedal).
scoreboard players operation #orc_last_pin ci_ambient = #orc_active ci_ambient
scoreboard players set #orc_active ci_ambient 0
scoreboard players set #orc_raised ci_ambient 0
function cobblemon_initiative:orc/cleared_ceremony
# a6: reward rolls scale with the ModMenu OrcConfig spoilsRolls (#cfg_orc_spoils_rolls — pushed by
# DojoDifficultyManager on server start / config save, init 1 in orc/load). Roll 1 keeps the pin
# tier (major P2-4 / minor P5-9); each extra roll (2..5) adds a major bundle. 0 = no reward.
execute if score #cfg_orc_spoils_rolls ci_ambient matches 1.. if score #orc_last_pin ci_ambient matches 2..4 run loot give @a[distance=..64,limit=1,sort=nearest] loot cobblemon_initiative:npc_gift/orc_spoils_major
execute if score #cfg_orc_spoils_rolls ci_ambient matches 1.. if score #orc_last_pin ci_ambient matches 5..9 run loot give @a[distance=..64,limit=1,sort=nearest] loot cobblemon_initiative:npc_gift/orc_spoils_minor
execute if score #cfg_orc_spoils_rolls ci_ambient matches 2.. run loot give @a[distance=..64,limit=1,sort=nearest] loot cobblemon_initiative:npc_gift/orc_spoils_major
execute if score #cfg_orc_spoils_rolls ci_ambient matches 3.. run loot give @a[distance=..64,limit=1,sort=nearest] loot cobblemon_initiative:npc_gift/orc_spoils_major
execute if score #cfg_orc_spoils_rolls ci_ambient matches 4.. run loot give @a[distance=..64,limit=1,sort=nearest] loot cobblemon_initiative:npc_gift/orc_spoils_major
execute if score #cfg_orc_spoils_rolls ci_ambient matches 5.. run loot give @a[distance=..64,limit=1,sort=nearest] loot cobblemon_initiative:npc_gift/orc_spoils_major

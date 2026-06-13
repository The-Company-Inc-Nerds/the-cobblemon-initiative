# Derive the current objective from existing state (run as @s = player). No new quest state:
# reads memory_fragment (= gyms beaten), defeated_* tags, and fields_liberated.
# Priority ladder is evaluated low -> high so the highest-priority branch wins.

# Keep the boss bar pointed at this player and the main sidebar line present (top of list).
bossbar set cobblemon_initiative:objective players @a
scoreboard players set #main ci_quest 100

# Count cleared Board members (scratch holder in quest_hud).
scoreboard players set #board quest_hud 0
execute if entity @s[tag=defeated_board_madeline] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_matt] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_micah] run scoreboard players add #board quest_hud 1
execute if entity @s[tag=defeated_board_lauren] run scoreboard players add #board quest_hud 1

# Default boss-bar progress = badges (memory_fragment / 10).
bossbar set cobblemon_initiative:objective max 10
execute store result bossbar cobblemon_initiative:objective value run scoreboard players get @s memory_fragment

# (0) GYM — next gym by badge count (memory_fragment 0..9).
execute if score @s memory_fragment matches ..9 run function cobblemon_initiative:quest/gym_town

# (1) HQ RAID — unlocked after gym 7, until Acting CEO DJ falls (climax outranks gyms 8-10).
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] run bossbar set cobblemon_initiative:objective name [{"text":"⚠ Raid Company HQ — Acting CEO DJ","color":"red"}]
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] run scoreboard players display name #main ci_quest [{"text":"▶ Raid Company HQ  [1590 51 1028]","color":"red"}]

# (2) ROYAL LEAGUE — all 10 badges, DJ down, champion still standing.
execute if score @s memory_fragment matches 10 if entity @s[tag=defeated_villain_boss] unless entity @s[tag=defeated_royal_champion] run bossbar set cobblemon_initiative:objective name [{"text":"★ Challenge the Royal League","color":"aqua"}]
execute if score @s memory_fragment matches 10 if entity @s[tag=defeated_villain_boss] unless entity @s[tag=defeated_royal_champion] run scoreboard players display name #main ci_quest [{"text":"▶ Challenge the Royal League","color":"aqua"}]

# (3) THE BOARD — champion down, board not yet fully cleared.
execute if entity @s[tag=defeated_royal_champion] if score #board quest_hud matches ..3 unless entity @s[tag=defeated_villain_final_boss] run bossbar set cobblemon_initiative:objective max 4
execute if entity @s[tag=defeated_royal_champion] if score #board quest_hud matches ..3 unless entity @s[tag=defeated_villain_final_boss] run execute store result bossbar cobblemon_initiative:objective value run scoreboard players get #board quest_hud
execute if entity @s[tag=defeated_royal_champion] if score #board quest_hud matches ..3 unless entity @s[tag=defeated_villain_final_boss] run bossbar set cobblemon_initiative:objective name [{"text":"☠ Hunt the Board of Directors","color":"dark_purple"}]
execute if entity @s[tag=defeated_royal_champion] if score #board quest_hud matches ..3 unless entity @s[tag=defeated_villain_final_boss] run scoreboard players display name #main ci_quest [{"text":"▶ Hunt the Board of Directors","color":"dark_purple"}]

# (4) DONE — The Founder has fallen.
execute if entity @s[tag=defeated_villain_final_boss] run bossbar set cobblemon_initiative:objective max 1
execute if entity @s[tag=defeated_villain_final_boss] run bossbar set cobblemon_initiative:objective value 1
execute if entity @s[tag=defeated_villain_final_boss] run bossbar set cobblemon_initiative:objective name [{"text":"⬛ The Company has fallen. Beyond the map: the Dragon.","color":"dark_green"}]
execute if entity @s[tag=defeated_villain_final_boss] run scoreboard players display name #main ci_quest [{"text":"▶ Hunt the Ender Dragon","color":"dark_green"}]

# ---- Side objectives (light up as their systems come online) ----
# Wheat War: shows "Liberate wheat fields n/6" once the war is flagged active (P4 sets wheat_war_active).
scoreboard players reset #side_wheat ci_quest
execute if entity @s[tag=wheat_war_active] run scoreboard players set #side_wheat ci_quest 80
execute if entity @s[tag=wheat_war_active] store result storage cobblemon_initiative:quest fields int 1 run scoreboard players get @s fields_liberated
execute if entity @s[tag=wheat_war_active] run function cobblemon_initiative:quest/set_wheat with storage cobblemon_initiative:quest

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

# (0b) OPENING CHAIN — Sango Town, pre-badge only. Mom (mom_sent_to_lab) -> starter at the
#      lab (chose_starter) -> Pokedex (got_pokedex) -> Running Shoes (got_running_shoes).
#      Overrides the gym line until the shoes are on; boss bar shows chain progress 0..4.
execute if score @s memory_fragment matches 0 unless entity @s[tag=got_running_shoes] run scoreboard players set #open quest_hud 0
execute if score @s memory_fragment matches 0 unless entity @s[tag=got_running_shoes] if entity @s[tag=mom_sent_to_lab] run scoreboard players add #open quest_hud 1
execute if score @s memory_fragment matches 0 unless entity @s[tag=got_running_shoes] if entity @s[tag=chose_starter] run scoreboard players add #open quest_hud 1
execute if score @s memory_fragment matches 0 unless entity @s[tag=got_running_shoes] if entity @s[tag=got_pokedex] run scoreboard players add #open quest_hud 1
execute if score @s memory_fragment matches 0 unless entity @s[tag=got_running_shoes] run bossbar set cobblemon_initiative:objective max 4
execute if score @s memory_fragment matches 0 unless entity @s[tag=got_running_shoes] store result bossbar cobblemon_initiative:objective value run scoreboard players get #open quest_hud
execute if score @s memory_fragment matches 0 unless entity @s[tag=mom_sent_to_lab] run bossbar set cobblemon_initiative:objective name [{"text":"⌂ Find your feet — talk to Mom","color":"yellow"}]
execute if score @s memory_fragment matches 0 unless entity @s[tag=mom_sent_to_lab] run scoreboard players display name #main ci_quest [{"text":"▶ Talk to Mom","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=mom_sent_to_lab] unless entity @s[tag=chose_starter] run bossbar set cobblemon_initiative:objective name [{"text":"⌂ Choose a partner at the Sango lab","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=mom_sent_to_lab] unless entity @s[tag=chose_starter] run scoreboard players display name #main ci_quest [{"text":"▶ Visit Professor Acacia at the lab","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=chose_starter] unless entity @s[tag=got_pokedex] run bossbar set cobblemon_initiative:objective name [{"text":"⌂ Take the Pokedex from Acacia","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=chose_starter] unless entity @s[tag=got_pokedex] run scoreboard players display name #main ci_quest [{"text":"▶ Take the Pokedex from Acacia","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=got_pokedex] unless entity @s[tag=got_running_shoes] run bossbar set cobblemon_initiative:objective name [{"text":"⌂ Show Mom your Pokedex","color":"yellow"}]
execute if score @s memory_fragment matches 0 if entity @s[tag=got_pokedex] unless entity @s[tag=got_running_shoes] run scoreboard players display name #main ci_quest [{"text":"▶ Show Mom your Pokedex","color":"yellow"}]

# (1) HQ RAID — after gym 7 AND 4 liberated fields (the hard gate: starve the monopoly
#     before storming it; DJ refuses the meeting while the fields still feed the Company),
#     until Acting CEO DJ falls (climax outranks gyms 8-10).
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] unless score @s fields_liberated matches 4.. run bossbar set cobblemon_initiative:objective name [{"text":"⚠ Starve the monopoly — liberate 4 wheat fields","color":"gold"}]
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] unless score @s fields_liberated matches 4.. run scoreboard players display name #main ci_quest [{"text":"▶ Liberate wheat fields, then raid HQ","color":"gold"}]
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] if score @s fields_liberated matches 4.. run bossbar set cobblemon_initiative:objective name [{"text":"⚠ Raid Company HQ — Acting CEO DJ","color":"red"}]
execute if score @s memory_fragment matches 7.. unless entity @s[tag=defeated_villain_boss] if score @s fields_liberated matches 4.. run scoreboard players display name #main ci_quest [{"text":"▶ Raid Company HQ  [1590 51 1028]","color":"red"}]

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
# Wheat War: shows the fields side line once the war is active AND the reveal has landed
# (heard_wheat_pitch — set by the Hua Zhan traders or the greenhouse catwalk; canon: the
# word never prints before the reveal).
scoreboard players reset #side_wheat ci_quest
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] run scoreboard players set #side_wheat ci_quest 80
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] store result storage cobblemon_initiative:quest fields int 1 run scoreboard players get @s fields_liberated
execute if entity @s[tag=wheat_war_active,tag=heard_wheat_pitch] run function cobblemon_initiative:quest/set_wheat with storage cobblemon_initiative:quest

# ── beat-2 side objectives (appended by the quest build) ──
# Four Gardens Pilgrimage: Garden seals n/4 side line — lights at the first seal, clears on the blessing (pilgrimage_done latch). Counting + macro render live in sidequest/pilgrimage/hud (mirrors the #side_wheat block above; #side_pilgrim rides ci_quest 78, below wheat at 80).
function cobblemon_initiative:sidequest/pilgrimage/hud

# Price Check (Hua Zhan side quest): shows Price checks noted n/3 once Kaito hands out the check (hz_price_check_active), hides after turn-in (hz_prices_done). Scratch counter #prices quest_hud per the render ladder pattern; macro lives in sidequest/price_check/set_prices (quest/ dir is shared).
scoreboard players reset #side_prices ci_quest
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] run scoreboard players set #prices quest_hud 0
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] if entity @s[tag=hz_price_1] run scoreboard players add #prices quest_hud 1
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] if entity @s[tag=hz_price_2] run scoreboard players add #prices quest_hud 1
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] if entity @s[tag=hz_price_3] run scoreboard players add #prices quest_hud 1
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] run scoreboard players set #side_prices ci_quest 74
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] store result storage cobblemon_initiative:quest prices int 1 run scoreboard players get #prices quest_hud
execute if entity @s[tag=hz_price_check_active] unless entity @s[tag=hz_prices_done] run function cobblemon_initiative:sidequest/price_check/set_prices with storage cobblemon_initiative:quest

scoreboard players reset #side_minutes ci_quest
execute if entity @s[tag=hz_minutes_heard,tag=!hz_minutes_filed] run scoreboard players set #side_minutes ci_quest 78
execute if entity @s[tag=hz_minutes_heard,tag=!hz_minutes_filed] run scoreboard players display name #side_minutes ci_quest [{"text":"• Deliver the minutes to Lucian in Sango","color":"gray"}]

# Verified Growth (reveal spine): tour pointer until the catwalk reveal lands.
scoreboard players reset #side_green ci_quest
execute if entity @s[tag=hz_arrived,tag=!wheat_named] run scoreboard players set #side_green ci_quest 79
execute if entity @s[tag=hz_arrived,tag=!wheat_named] run scoreboard players display name #side_green ci_quest [{"text":"• Tour the Company greenhouse","color":"gray"}]

# Grain In, Goods Out (the Miller Walk): survey line while active.
scoreboard players reset #side_survey ci_quest
execute if entity @s[tag=hz_survey_active,tag=!hz_survey_paid] run scoreboard players set #side_survey ci_quest 76
execute if entity @s[tag=hz_survey_active,tag=!hz_survey_paid] run scoreboard players display name #side_survey ci_quest [{"text":"• Survey the grain market for the miller","color":"gray"}]

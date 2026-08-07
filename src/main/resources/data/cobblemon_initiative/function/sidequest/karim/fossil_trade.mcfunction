# Karim Zahur's fossil trade (Kalahar SQ, playtest 2026-08-06 N19). Run AS the player
# (Karim's trade button, ExecAsUser via the allowlisted `function` root). ONE-SHOT: the
# stonemason takes ONE fossil he can read the maker's-mark on and, in return, hands over a
# desert creature that was never in that stone at all — a Trapinch, the antlion of the dunes.
# First-match probe over all 11 fossil item ids (jar-verified vs Cobblemon 1.7.3), match order
# = the museum machine's order (turnin/use_machine precedent). #karim ci_item holds the carried
# count under test; the first non-zero count consumes ONE, gives Trapinch and returns. NO
# repeatable payout (hardcore no-farm rule) — the karim_traded latch closes the trade after one.
execute if entity @s[tag=karim_traded] run title @s actionbar [{"text":"You already carried off my desert one. A stonemason trades a stone once.","color":"gray"}]
execute if entity @s[tag=karim_traded] run return 0
scoreboard players set #karim ci_item 0
execute store result score #karim ci_item run clear @s cobblemon:dome_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:dome_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:helix_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:helix_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:old_amber_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:old_amber_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:root_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:root_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:claw_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:claw_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:skull_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:skull_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:armor_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:armor_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:cover_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:cover_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:plume_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:plume_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:jaw_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:jaw_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
execute store result score #karim ci_item run clear @s cobblemon:sail_fossil 0
execute if score #karim ci_item matches 1.. run clear @s cobblemon:sail_fossil 1
execute if score #karim ci_item matches 1.. run function cobblemon_initiative:sidequest/karim/fossil_trade_grant
execute if score #karim ci_item matches 1.. run return 1
# Nothing matched — the player carries no fossil for Karim to read.
title @s actionbar [{"text":"Bring me a fossil — any of them. I trade the stone for something the desert still keeps.","color":"red"}]

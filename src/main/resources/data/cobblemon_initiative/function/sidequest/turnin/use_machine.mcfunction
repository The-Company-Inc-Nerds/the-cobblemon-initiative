# Resurrection machine — ONE-BUTTON flow (2026-08-03 playtest N1). Run as @s = the player
# (Kenji's machine_button, ExecAsUser via the allowlisted `function` root).
# Finds WHICHEVER fossil the player carries — first match over all 11 — and hands off to that
# fossil's literal turnin (which re-probes, consumes 1, tags telemetry + carry tags, floats the
# fossil over the platform and schedules the revive cinematic). Match order = the old menu order.
# The per-species sq_revived_<sp> lockout is GONE: the tank re-wakes any fossil type repeatedly;
# the one-revive-at-a-time busy-guard below is the only gate (a 2nd revive mid-cinematic would
# consume a fossil the in-flight revive_finish then discards — same load-bearing guard as before).
execute if entity @s[tag=ci_reviving] run title @s actionbar [{"text":"The tank is still working - wait for it to finish.","color":"red"}]
execute if entity @s[tag=ci_reviving] run return 0
# First-match probe chain. #machine ci_item holds the carried count of the fossil under test;
# the first non-zero count dispatches that turnin and returns. ci_item is created by turnin/load.
scoreboard players set #machine ci_item 0
execute store result score #machine ci_item run clear @s cobblemon:dome_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/dome_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:helix_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/helix_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:old_amber_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/old_amber_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:root_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/root_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:claw_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/claw_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:skull_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/skull_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:armor_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/armor_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:cover_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/cover_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:plume_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/plume_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:jaw_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/jaw_fossil
execute if score #machine ci_item matches 1.. run return 1
execute store result score #machine ci_item run clear @s cobblemon:sail_fossil 0
execute if score #machine ci_item matches 1.. run function cobblemon_initiative:sidequest/turnin/sail_fossil
execute if score #machine ci_item matches 1.. run return 1
# Nothing matched — the player carries no fossil the tank recognizes.
title @s actionbar [{"text":"You carry no fossil the tank recognizes. Brush the soft gravel and bring one back.","color":"red"}]

# Memory Fragment — badge-gated memory drip
# Called from a gym leader's "command" reward (TrainerConfig.RewardConfig type=command).
# Runs at permission level 4 (PlayerProgressManager.executeCommand -> withPermission(4)).
#
# Usage from trainer JSON reward:
#   { "type": "command",
#     "command": "execute as {player} run function cobblemon_initiative:memory/grant_fragment {n:1,title:'...',sub:'...'}" }
#
# {player} -> player name, {uuid} -> player UUID (only tokens TrainerConfig supports).
# We re-target with `execute as {player}` so @s inside this macro is the player.
#
# Params:
#   n     = fragment index 1..10 (sets the PLAYER_TAG memory_fragment_<n> for the re-reader NPC)
#   title = first-person title line (already styled JSON-safe text, no double quotes)
#   sub   = subtitle line
#
# RELOG-SAFE: one-way latch. The PlayerProgressManager already guards re-defeat
# (hasDefeatedTrainer short-circuits onTrainerDefeated), so this fires exactly once
# per leader per world. The PLAYER_TAG persists in world data, so the re-reader NPC
# still works after relog without re-firing the title.

$tag @s add memory_fragment_$(n)
$scoreboard players set @s memory_fragment $(n)

# Cinematic delivery: clear any lingering title, set styled title + subtitle, then show.
title @s times 10 70 20
$title @s subtitle {"text":"$(sub)","color":"dark_gray","italic":true}
$title @s title {"text":"$(title)","color":"#7A5CA8","bold":true}

# Subtle "shadow self / memory surfacing" sound — quiet, low pitch.
playsound minecraft:block.sculk_sensor.clicking master @s ~ ~ ~ 0.6 0.5
playsound minecraft:particle.soul_escape master @s ~ ~ ~ 0.4 0.8

# Chat echo so the line is re-readable in the log during the stream.
$tellraw @s [{"text":"[Memory] ","color":"#7A5CA8","bold":true},{"text":"$(title)","color":"gray","italic":true}]

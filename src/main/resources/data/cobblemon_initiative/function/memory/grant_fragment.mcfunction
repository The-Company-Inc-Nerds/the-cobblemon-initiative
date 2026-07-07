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
#
# TWO-STAGE CEREMONY (2026-07-06): the tags/score latch immediately, but the eerie
# purple title is DEFERRED 4s (storage + schedule -> memory/frag_title) so the badge
# triumph layer (rewards/badge_ceremony, fireworks + toast) gets the screen first and
# the memory beat lands alone in the silence after. Single-player contract: the
# schedule callback re-targets via @a (the band_tags precedent).

$tag @s add memory_fragment_$(n)
$scoreboard players set @s memory_fragment $(n)

# Stage the fragment text and schedule the reveal (replace semantics — two badges can
# never land within 4s of each other).
$data merge storage cobblemon_initiative:memory {frag:{title:"$(title)",sub:"$(sub)"}}
schedule function cobblemon_initiative:memory/frag_title 4s

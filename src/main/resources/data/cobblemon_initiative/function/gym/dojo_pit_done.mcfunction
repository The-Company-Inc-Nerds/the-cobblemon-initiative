# Deep pit CLEARED (a22 PVP) — Ken is down (absence poll, no dc_ken_hostile near the pit). Latch
# 2->3 FIRST (never re-credits), grant defeated_deepcore_apprentice — the tag Bruno's "ready" gate
# reads to offer the Iron Badge fight (Bruno stays a Cobblemon leader battle, out of scope for the
# PVP note). Single-player: @a is the one challenger.
scoreboard players set #dc_pit_stage ci_gym 3
tag @a add defeated_deepcore_apprentice
title @a[distance=..48] title [{"text":"THE PIT IS CLEARED","color":"gold","bold":true}]
title @a[distance=..48] subtitle [{"text":"Bruno will see you now","color":"yellow"}]
playsound minecraft:ui.toast.challenge_complete master @a[distance=..48] ~ ~ ~ 1 1

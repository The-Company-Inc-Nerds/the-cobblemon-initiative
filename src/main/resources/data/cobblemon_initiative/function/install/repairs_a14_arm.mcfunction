# repairs wave a14 — arm: duplicate cobblemon-model companions, TAKE TWO (0.6.0-alpha.14).
# a13 tried to clear the Mimi/Jackpot/Coins/Bobber/Cloud/Pip dupes by killing
# `@e[type=easy_npc:cobblemon_npc,name="Mimi"]` &c. GLOBALLY — but Easy NPC stores the body
# name as a JSON text component, so the bare `name="Mimi"` string match no-opped, every stale
# body survived, and a13's latch re-arm spawned YET ANOTHER copy (so a save that saw a13 may
# now hold 2-3 bodies each). a13 also already burned its #repair_a13 guard, so it will not retry.
# FIX (a14): forget names entirely. Forceload each companion's home, then (in apply) kill EVERY
# easy_npc:cobblemon_npc within a tight radius of that home — name-agnostic, so it clears 1/2/3
# copies alike — and re-arm the single latch. Proximity (not global) so noble cobblemon_npc
# bodies at the far monuments/arenas are never touched. Homes are 38+ blocks apart, radius 24 <
# half the nearest gap, so no companion's sweep reaches its neighbour.
# Guards itself via #repair_a14 so it applies exactly once per world.
scoreboard players set #repair_a14 ci_ambient 1

# Mimi (Mr. Mime) — home 2605.5 109 2846.5 (Sango)
forceload add 2576 2816
forceload add 2576 2832
forceload add 2576 2848
forceload add 2592 2816
forceload add 2592 2832
forceload add 2592 2848
forceload add 2608 2816
forceload add 2608 2832
forceload add 2608 2848
# Jackpot (Magikarp) — home 2568.5 111 2855.5 (Sango)
forceload add 2544 2832
forceload add 2544 2848
forceload add 2544 2864
forceload add 2560 2832
forceload add 2560 2848
forceload add 2560 2864
# Pip (Sentret) — home 2588.5 107 2957.5 (Sango)
forceload add 2560 2928
forceload add 2560 2944
forceload add 2560 2960
forceload add 2576 2928
forceload add 2576 2944
forceload add 2576 2960
forceload add 2592 2928
forceload add 2592 2944
forceload add 2592 2960
# Coins (Meowth) — home 1543.5 88 2109.5 (Hua Zhan)
forceload add 1520 2096
forceload add 1536 2096
forceload add 1552 2096
forceload add 1536 2112
forceload add 1552 2112
# Cloud (Wooloo) — home 1513.5 84 1988.5 (Hua Zhan)
forceload add 1488 1984
forceload add 1504 1984
forceload add 1520 1984
forceload add 1504 2000
forceload add 1520 2000
# Bobber (Psyduck) — home 1893.5 105 2470.5 (Deepcore road / Kalahar approach)
forceload add 1872 2464
forceload add 1888 2464
forceload add 1904 2464
forceload add 1888 2448
forceload add 1888 2480
# Hua Zhan decorative statue props RELEASED (2026-07-21, showrunner): the four person-warden
# statues (Lin/Mei/Fang/Xiu) ARE the garden wardens; these 4 decorative props (Knight/Traveller/
# Climber/Gardener) were redundant → despawn them. Forceload all four prop chunks so the apply
# proximity kills reach the bodies.
forceload add 1472 2096
forceload add 1472 2112
forceload add 1440 2016
forceload add 1456 2096
forceload add 1536 2016

schedule function cobblemon_initiative:install/repairs_a14_apply 3s

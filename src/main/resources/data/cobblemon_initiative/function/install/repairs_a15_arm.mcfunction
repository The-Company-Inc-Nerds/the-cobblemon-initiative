# repairs wave a15 — arm: cobblemon-model companion dupes FINAL sweep + Victor relocation
# (0.6.0-alpha.15).
# a14 cleared the Mimi/Jackpot/Coins/Bobber/Cloud/Pip dupes by proximity+type kill and
# re-armed each latch, but it is one-shot (#repair_a14) and in saves where its 3s-scheduled
# apply ran with the home chunk still unloaded the kill no-opped — so two Mimis / two
# Jackpots persist on alpha.14. This wave re-sweeps under a fresh guard AND ships with the
# recompiled placement latches, whose new dedup guard (`unless entity` in the tick +
# kill-before-import in each place fn) makes a re-dupe impossible. This is the LAST
# companion repair. Forceload each home so the apply's kill runs with the body live, then
# (in apply) kill EVERY easy_npc:cobblemon_npc within 24 of that home — name/tag-agnostic,
# clears 1/2/3 copies alike — and reset the single latch. Proximity (not global) so noble
# cobblemon_npc bodies at the far monuments/arenas are never touched.
# Guards itself via #repair_a15 so it applies exactly once per world.
scoreboard players set #repair_a15 ci_ambient 1

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
# Victor (Sango) — RELOCATED from the grain-tower top (2522.6/131/2815.5) to his reveal
# site (2536/106/2900). Forceload the OLD tower chunks so the apply's kill of the stale
# apprentice body lands; the re-armed latch then re-spawns him at the new coords on the
# next visit. (The new site is a normal latch — no forceload needed there.)
forceload add 2512 2800
forceload add 2512 2816
forceload add 2528 2800
forceload add 2528 2816

schedule function cobblemon_initiative:install/repairs_a15_apply 3s

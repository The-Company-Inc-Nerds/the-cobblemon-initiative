# repairs wave a37 — arm: HQ act-2 entrance cleanup (playtest 2026-08-06). Scope: sweep the
# 12 retired Cyber-City prop/quest bodies. Three now-propless sidequests were RETIRED entirely:
#   Off the Records  — Maren (giver) + 3 archive drops (props)
#   Signal Integrity — Rell (giver) + 3 glitching billboards (board_1/2/3, props)
#   Exchange Rate    — the Verified Value Teller (giver) + 3 reserve tags (reserve_1/2/3, props)
# All 12 are easy_npc:humanoid latch bodies (no uuid). ExecAsUser kill is dropped
# (ENGINE_FINDINGS §3) — cut bodies die via proximity+type kills in the apply. No name= guard
# (Easy NPC CustomName is a JSON component name= never matches). No KEPT humanoid is within 4
# blocks of any cut spawn (proximity-verified: nearest kept is Kessler @1621.5/92/1116.5, ~4.24
# from reserve_3@1614.5; cyber_grid_broker @1555.5, ~16.8 from board_2/reserve_2), so
# distance=..2 is disjoint. Also RESET each #amb_* latch so a recompile without the (now-deleted)
# char can never respawn — belt-and-braces; the place fns are deleted too. Guarded once-per-world
# by #repair_a37 like every wave.
scoreboard players set #repair_a37 ci_ambient 1
# Records annex cluster (Maren + 3 archive drops), x1512..1524 z1092..1104 — one block box.
forceload add 1512 1092 1524 1104
# Downtown SW props/givers: reserve_1 + board_1 @1490 z1128/1132; teller @1500 z1120; Rell @1470 z1140.
forceload add 1470 1120 1500 1140
# Downtown NE kept-quest props board_2/reserve_2 @1560 z1090/1092.
forceload add 1560 1090 1560 1092
# HQ-entrance props board_3/reserve_3 @1614/1624 z1119 + a34 street-side spots @1610 z1110/1112.
forceload add 1610 1110 1624 1119
schedule function cobblemon_initiative:install/repairs_a37_apply 3s

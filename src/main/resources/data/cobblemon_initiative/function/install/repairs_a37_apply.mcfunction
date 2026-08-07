# repairs wave a37 — apply: sweep the 12 retired Cyber prop/quest bodies (chunks forceloaded by
# the arm). Proximity+type kills (easy_npc:humanoid, distance=..2) — no name= guard; every cut
# spawn is >4 blocks from every kept humanoid so the radius is collision-safe. Latch reset
# alongside each kill so no future recompile can respawn a deleted char.

# ── Off the Records (RETIRED): Maren (giver) + 3 archive drops (props) ──
execute positioned 1520.5 89 1100.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_defector_maren ci_ambient
execute positioned 1518.5 89 1096.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_archive_1 ci_ambient
execute positioned 1524.5 90 1104.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_archive_2 ci_ambient
execute positioned 1512.5 89 1092.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_archive_3 ci_ambient

# ── Signal Integrity (RETIRED): Rell (giver) + 3 glitching billboards (props) ──
execute positioned 1470.5 89 1140.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_signal_tech ci_ambient
execute positioned 1490.5 90 1132.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_board_1 ci_ambient
execute positioned 1560.5 91 1090.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_board_2 ci_ambient
# board_3: current spawn 1624.5 + a34 street-side spot 1610.5 (the a34 move never took — kill both).
execute positioned 1624.5 92 1119.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 1610.5 92 1110.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_board_3 ci_ambient

# ── Exchange Rate (RETIRED): the Verified Value Teller (giver) + 3 reserve tags (props) ──
execute positioned 1500.5 91 1120.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_exchange_teller ci_ambient
execute positioned 1490.5 91 1128.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_reserve_1 ci_ambient
execute positioned 1560.5 93 1092.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_reserve_2 ci_ambient
# reserve_3: current spawn 1614.5 + a34 street-side spot 1610.5 z1112.5 (a34 move never took — kill both).
execute positioned 1614.5 92 1119.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 1610.5 92 1112.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players reset #amb_cyber_reserve_3 ci_ambient

# ── teardown ──
forceload remove 1512 1092 1524 1104
forceload remove 1470 1120 1500 1140
forceload remove 1560 1090 1560 1092
forceload remove 1610 1110 1624 1119

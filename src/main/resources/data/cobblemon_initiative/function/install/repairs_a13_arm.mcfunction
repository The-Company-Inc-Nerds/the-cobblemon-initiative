# repairs wave a13 — arm: duplicate cobblemon-model companions (0.6.0-alpha.13).
# ROOT CAUSE: the a10 dialog-cohesion wave killed the six cobblemon-model companions
# (Mimi/Jackpot/Coins/Bobber/Cloud/Pip) with `type=easy_npc:humanoid`, but these bodies
# are `type=easy_npc:cobblemon_npc` — so those kills matched NOTHING and no-opped, yet
# a10 still reset each #amb_* latch flag to 0. Next tick the untouched original body was
# still alive AND the latch re-imported a SECOND body → visible duplicates.
# FIX: kill the stale body by the CORRECT type (belt-and-suspenders: by NAME to catch the
# existing save bodies AND by entity-tag to catch a nickname-renamed or wandered body),
# THEN reset the latch so exactly ONE fresh body re-imports on the next visit.
# Guards itself via #repair_a13 so it applies exactly once per world.
scoreboard players set #repair_a13 ci_ambient 1

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
forceload add 2576 2832
forceload add 2576 2848
forceload add 2576 2864
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
forceload add 1520 2080
forceload add 1520 2096
forceload add 1520 2112
forceload add 1536 2080
forceload add 1536 2096
forceload add 1536 2112
forceload add 1552 2080
forceload add 1552 2096
forceload add 1552 2112
# Cloud (Wooloo) — home 1513.5 84 1988.5 (Hua Zhan)
forceload add 1488 1968
forceload add 1488 1984
forceload add 1488 2000
forceload add 1504 1968
forceload add 1504 1984
forceload add 1504 2000
forceload add 1520 1968
forceload add 1520 1984
forceload add 1520 2000
# Bobber (Psyduck) — home 1893.5 105 2470.5 (Deepcore road / Kalahar approach)
forceload add 1872 2448
forceload add 1872 2464
forceload add 1872 2480
forceload add 1888 2448
forceload add 1888 2464
forceload add 1888 2480
forceload add 1904 2448
forceload add 1904 2464
forceload add 1904 2480
# Tomo (Beekeeper, note 13 removal) — persisted body 1920 105 2570 (Takehara / Blossom arch)
forceload add 1904 2560
forceload add 1920 2560
forceload add 1936 2560
forceload add 1904 2576
forceload add 1920 2576
forceload add 1936 2576

schedule function cobblemon_initiative:install/repairs_a13_apply 3s

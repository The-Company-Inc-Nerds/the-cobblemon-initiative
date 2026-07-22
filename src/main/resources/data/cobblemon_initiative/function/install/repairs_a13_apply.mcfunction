# repairs wave a13 — apply: kill the DUPLICATE cobblemon-model companion bodies by the
# CORRECT easy_npc type, then re-arm each latch so exactly ONE fresh body re-imports.
# Kills are GLOBAL (no proximity): the a10 name-kills used type=easy_npc:humanoid and
# matched nothing, so both the original and the latch-spawned copy are still alive.
# type=easy_npc:cobblemon_npc pins the query to companion bodies (never a player's
# Cobblemon, never a humanoid NPC), so a bare name/tag match is safe. We kill by NAME
# (the existing save bodies) AND by entity-tag (a nickname-renamed or wandered body).
# Mimi carries ci_mimi_body; Jackpot carries ci_jackpot_body (added a13). The four
# unTagged companions are killed by name only (belt where suspenders don't yet exist).

# Mimi (Mr. Mime) — kill both duplicate bodies, re-arm #amb_mrmime
kill @e[type=easy_npc:cobblemon_npc,name="Mimi"]
kill @e[type=easy_npc:cobblemon_npc,tag=ci_mimi_body]
scoreboard players set #amb_mrmime ci_ambient 0
# Jackpot (Magikarp) — re-arm #amb_magikarp
kill @e[type=easy_npc:cobblemon_npc,name="Jackpot"]
kill @e[type=easy_npc:cobblemon_npc,tag=ci_jackpot_body]
scoreboard players set #amb_magikarp ci_ambient 0
# Coins (Meowth) — re-arm #amb_meowth
kill @e[type=easy_npc:cobblemon_npc,name="Coins"]
scoreboard players set #amb_meowth ci_ambient 0
# Bobber (Psyduck) — re-arm #amb_psyduck
kill @e[type=easy_npc:cobblemon_npc,name="Bobber"]
scoreboard players set #amb_psyduck ci_ambient 0
# Cloud (Wooloo) — re-arm #amb_wooloo
kill @e[type=easy_npc:cobblemon_npc,name="Cloud"]
scoreboard players set #amb_wooloo ci_ambient 0
# Pip (Sentret) — re-arm #amb_sentret
kill @e[type=easy_npc:cobblemon_npc,name="Pip"]
scoreboard players set #amb_sentret ci_ambient 0

# Beekeeper Tomo (note 13 removal) — despawn the persisted world body by uuid; its dialog
# + quest are gone and the uuid->preset mapping was pruned, so it will not re-import.
easy_npc delete 4e3a8cf1-e57f-4666-aed5-4e51f8b6b020
kill @e[type=easy_npc:humanoid,tag=sq_beekeeper_tomo]

forceload remove 2576 2816
forceload remove 2576 2832
forceload remove 2576 2848
forceload remove 2592 2816
forceload remove 2592 2832
forceload remove 2592 2848
forceload remove 2608 2816
forceload remove 2608 2832
forceload remove 2608 2848
forceload remove 2544 2832
forceload remove 2544 2848
forceload remove 2544 2864
forceload remove 2560 2832
forceload remove 2560 2848
forceload remove 2560 2864
forceload remove 2576 2832
forceload remove 2576 2848
forceload remove 2576 2864
forceload remove 2560 2928
forceload remove 2560 2944
forceload remove 2560 2960
forceload remove 2576 2928
forceload remove 2576 2944
forceload remove 2576 2960
forceload remove 2592 2928
forceload remove 2592 2944
forceload remove 2592 2960
forceload remove 1520 2080
forceload remove 1520 2096
forceload remove 1520 2112
forceload remove 1536 2080
forceload remove 1536 2096
forceload remove 1536 2112
forceload remove 1552 2080
forceload remove 1552 2096
forceload remove 1552 2112
forceload remove 1488 1968
forceload remove 1488 1984
forceload remove 1488 2000
forceload remove 1504 1968
forceload remove 1504 1984
forceload remove 1504 2000
forceload remove 1520 1968
forceload remove 1520 1984
forceload remove 1520 2000
forceload remove 1872 2448
forceload remove 1872 2464
forceload remove 1872 2480
forceload remove 1888 2448
forceload remove 1888 2464
forceload remove 1888 2480
forceload remove 1904 2448
forceload remove 1904 2464
forceload remove 1904 2480
forceload remove 1904 2560
forceload remove 1920 2560
forceload remove 1936 2560
forceload remove 1904 2576
forceload remove 1920 2576
forceload remove 1936 2576
execute if score #debug ci_ambient matches 1 run tellraw @a [{"text":"[Initiative] ","color":"gold"},{"text":"Repair a13: cobblemon-model companion dupes cleared — Mimi/Jackpot & co. re-latch as singles.","color":"gray"}]

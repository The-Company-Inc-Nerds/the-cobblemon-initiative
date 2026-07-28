# repairs wave a19 — apply (audit rulings). a17/a18 kill shapes.

# Tunde — podium dupe deleted + agent re-voice (r3 keeps Musa safe, 13 blocks off)
execute positioned 2581.5 111.0 2822.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_sango_company_liaison ci_ambient 0

# Auditors Bomani/Jelani — dead sweep entry removals
execute positioned 2611.5 110.0 2792.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_auditor_a ci_ambient 0
execute positioned 2578.5 108.0 2942.5 run kill @e[type=easy_npc:humanoid,distance=..3]
scoreboard players set #amb_auditor_b ci_ambient 0

# Dune — the renamed Kalahar hippo (dedup tag catches wander drift)
kill @e[type=easy_npc:cobblemon_npc,tag=ci_amb_companion_kalahar_hippopotas]
scoreboard players set #amb_kalahar_hippopotas ci_ambient 0

# Ghost-cast save hygiene (harness/scenario worlds only; live players never held these)
scoreboard objectives remove ci_notices
tag @a remove notices_filed
tag @a remove notice_1
tag @a remove notice_2
tag @a remove notice_3
tag @a remove file_refiled
tag @a remove memo_heard
tag @a remove memo_delivered
tag @a remove memo_loiter
tag @a remove letter_surrendered
tag @a remove paid_checkpoint_fee
tag @a remove paid_handling_fee
tag @a remove checkpoint_settled

forceload remove 2560 2780 2630 2950
forceload remove 2070 3940 2090 3956

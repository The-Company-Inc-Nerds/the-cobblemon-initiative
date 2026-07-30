# repairs wave a28 — apply (chunks live, 3s after arm).
# Kill the a27 EYESIGHT Jr. Apprentice / Apprentice bodies (at their ring spots) + Bram's old
# deep-pier body, reset the latches so the recompiled presets re-spawn — jr/apprentice as
# talk-to-battle at the SAME spots, Bram at his new 436.5/64/3491.5 home. Radius 2 keeps Leader
# Neptune (~596/87/3646) and the four crew trainers clear of the sweep.
execute positioned 560.5 92 3646.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 620.5 92 3646.5 run kill @e[type=easy_npc:humanoid,distance=..2]
execute positioned 605.5 87 3650.5 run kill @e[type=easy_npc:humanoid,distance=..2]
scoreboard players set #amb_gaviota_jr_apprentice ci_ambient 0
scoreboard players set #amb_gaviota_apprentice ci_ambient 0
scoreboard players set #amb_gaviota_manifest_c ci_ambient 0
forceload remove 555 3640 625 3655

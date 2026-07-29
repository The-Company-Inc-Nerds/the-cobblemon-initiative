# Raise the active orc camp (a22). Run POSITIONED at the active pin from orc/tick when a player
# comes within 48 (chunk-load guard). Latch #orc_raised 0->1 FIRST (one-shot). Warband = 3 Orc
# Raiders (easy_npc:orc) + 1 Orc Warrior (easy_npc:orc_warrior) + 1 Orc War-Chief, import_new'd from
# their presets at ~ offsets around the pin, +1 y (night_watch idiom — settle, not suffocate). Their
# attack-on-sight AI + iron gear live in the presets; entity_tags ci_orc is the cleared-poll target.
# VERIFY in-world: `easy_npc preset import_new` resolves ~ relative coords under `positioned` (if not,
# swap to per-pin absolute); easy_npc orcs spawn, render, aggro on sight, and are killable, no crash.
scoreboard players set #orc_raised ci_ambient 1
title @a[distance=..48] actionbar [{"text":"War-drums on the ridge — a raider camp stirs","color":"red"}]
playsound minecraft:event.raid.horn master @a[distance=..48] ~ ~ ~ 1 0.8
easy_npc preset import_new data easy_npc:preset/orc/orc_raider.npc.snbt ~4 ~1 ~3
easy_npc preset import_new data easy_npc:preset/orc/orc_raider.npc.snbt ~-5 ~1 ~2
easy_npc preset import_new data easy_npc:preset/orc/orc_raider.npc.snbt ~3 ~1 ~-4
easy_npc preset import_new data easy_npc:preset/orc_warrior/orc_warrior.npc.snbt ~-3 ~1 ~-3
easy_npc preset import_new data easy_npc:preset/orc_warrior/orc_chief.npc.snbt ~0 ~1 ~-1

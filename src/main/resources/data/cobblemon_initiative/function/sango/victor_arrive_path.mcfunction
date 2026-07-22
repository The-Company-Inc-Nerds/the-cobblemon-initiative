# Victor arrives at the reveal path — the player sees him standing where he wasn't before, and
# something is about to happen. Spawn his body here exactly once (#amb_victor_path 0 -> 1), with
# a soft arrival cue so the relocation reads as intentional. Talking to him now offers the
# transform (dialog gated on victor_descended), which plays victini_reveal IN PLACE at this spot.
# The spawn spot 2536.5/106/2900.5 is PROVEN solid ground — it was Victor's alpha.15 placement,
# so he stood here live all last version. The latch fires at distance<=40, so the chunk is loaded
# and import_new lands (no re-arm self-heal needed — that would race the deferred import + dup him).
scoreboard players set #amb_victor_path ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/victor.npc.snbt 2536.5 106 2900.5
particle minecraft:end_rod 2536.5 106.9 2900.5 0.25 0.6 0.25 0.02 30 force
playsound minecraft:block.beacon.ambient master @a[distance=..30] 2536.5 106.6 2900.5 0.7 1.3

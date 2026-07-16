# scorchspire_healer — latch FIRST; reset #amb_scorchspire_healer to 0 (+ kill the body) to respawn.
scoreboard players set #amb_scorchspire_healer ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/scorchspire_healer.npc.snbt 3672.5 68 4576.5

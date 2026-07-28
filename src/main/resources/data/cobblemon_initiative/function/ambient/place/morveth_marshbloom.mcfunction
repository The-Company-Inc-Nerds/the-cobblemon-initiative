# morveth_marshbloom — latch FIRST; reset #amb_morveth_marshbloom to 0 (+ kill the body) to respawn.
scoreboard players set #amb_morveth_marshbloom ci_ambient 1
easy_npc preset import_new data easy_npc:preset/bogged/morveth_marshbloom.npc.snbt 1123.5 75 2335.5

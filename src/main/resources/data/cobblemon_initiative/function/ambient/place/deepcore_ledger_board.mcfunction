# deepcore_ledger_board — latch FIRST; reset #amb_deepcore_ledger_board to 0 (+ kill the body) to respawn.
scoreboard players set #amb_deepcore_ledger_board ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/deepcore_ledger_board.npc.snbt 1152.5 146 3284.5

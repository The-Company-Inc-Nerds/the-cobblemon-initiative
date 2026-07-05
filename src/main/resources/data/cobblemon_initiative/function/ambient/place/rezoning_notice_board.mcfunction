# rezoning_notice_board — latch FIRST; reset #amb_rezoning_notice_board to 0 (+ kill the body) to respawn.
scoreboard players set #amb_rezoning_notice_board ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/rezoning_notice_board.npc.snbt 1503.5 86 2041.5

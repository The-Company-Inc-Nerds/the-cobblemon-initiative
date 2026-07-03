# Run as/at a takehara_sentry the moment its can_see_player flips to 1 (latched).
scoreboard players set @s gym1_latch 1
scoreboard players add @p[distance=..24,tag=!perf_review_resolved] gym1_seen 1
title @p[distance=..24] actionbar [{"text":"EYES ON YOU","color":"red","bold":true},{"text":" — a sentry marked you. The engagement goes on the books.","color":"gray"}]
playsound minecraft:block.note_block.pling player @p[distance=..24] ~ ~ ~ 1 0.6

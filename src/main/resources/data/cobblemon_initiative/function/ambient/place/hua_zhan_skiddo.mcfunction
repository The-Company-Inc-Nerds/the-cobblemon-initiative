# companion_hua_zhan_skiddo — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_hua_zhan_skiddo — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_hua_zhan_skiddo to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_hua_zhan_skiddo]
scoreboard players set #amb_hua_zhan_skiddo ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_hua_zhan_skiddo.npc.snbt 1413.5 112 2054.5

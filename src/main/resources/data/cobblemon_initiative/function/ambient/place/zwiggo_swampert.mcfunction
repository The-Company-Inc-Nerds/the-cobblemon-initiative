# zwiggo_swampert — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_zwiggo_swampert — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_zwiggo_swampert to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_zwiggo_swampert]
scoreboard players set #amb_zwiggo_swampert ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/zwiggo_swampert.npc.snbt 406.8 64 3501.2

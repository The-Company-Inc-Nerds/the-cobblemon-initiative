# companion_mystic_morgrem — cobblemon-model latch NPC. Kill any pre-existing body carrying THIS
# NPC's unique dedup tag FIRST (ci_amb_companion_mystic_morgrem — never a co-located neighbour),
# THEN latch + import exactly one. Runs only with a player within 40 (chunk
# live) so the kill never no-ops. reset #amb_mystic_morgrem to 0 (+ kill the body) to
# respawn. (Legacy pre-tag dupes are cleared once by install/repairs_a15.)
kill @e[tag=ci_amb_companion_mystic_morgrem]
scoreboard players set #amb_mystic_morgrem ci_ambient 1
easy_npc preset import_new data easy_npc:preset/humanoid/companion_mystic_morgrem.npc.snbt 1213.5 87 2442.5

# MACRO — retire the active site (turn-in or re-mark): forceload, schedule the sand restore.
$forceload add $(cx) $(cz)
schedule function cobblemon_initiative:sidequest/reach_cache/clean_sched 2s

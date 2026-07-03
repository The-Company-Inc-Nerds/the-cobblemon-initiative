# Cascade Ascent — per-tick guard. Needs the #minecraft:tick tag (listed in the agent
# report; do NOT edit function tags directly per workflow rules).
# Cheap when idle: one selector check, nothing else runs unless a climb is live.
execute if entity @a[tag=ci_ascending] run function cobblemon_initiative:sidequest/cascade/tick_run

# MACRO — shared arm step: force-load the dig site's chunk so the scheduled placement lands on
# live blocks. Called by arm_kiln/arm_warden with the matching storage compound. The caller
# schedules its own place_<hunt> wrapper (+2s) because `schedule` strips macro args.
$forceload add $(cx) $(cz)

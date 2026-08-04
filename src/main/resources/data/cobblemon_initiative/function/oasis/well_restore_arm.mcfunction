# Kalahar TOWN WELL restore (a18, playtest marker M1): the moment the Oasis survey is spiked
# (sidequest/kalahar/shut_pump latches oasis_pump_off) the water comes home. Forceload-then-schedule
# — the player is at the Oasis, ~350 blocks from the well, so its chunks are unloaded.
forceload add 2068 4045 2078 4055
schedule function cobblemon_initiative:oasis/well_restore_apply 2s

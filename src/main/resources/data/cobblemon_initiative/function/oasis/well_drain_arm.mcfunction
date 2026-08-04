# Kalahar TOWN WELL drain (a18, playtest marker M1 'WellBlocks'): while the Company survey crew
# squats the Oasis, the town well at 2068-2078 / 125 / 4045-4055 stands DRY — Leila Safiya rations
# cups beside it. Called from install/repairs_a30_apply (guarded on no player having oasis_pump_off,
# so an already-cleared save keeps its water). Forceload-then-schedule because the well chunks are
# almost never loaded when repairs run (the repairs-wave idiom).
forceload add 2068 4045 2078 4055
schedule function cobblemon_initiative:oasis/well_drain_apply 2s

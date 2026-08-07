# Kalahar SECOND WELL fill (M35, playtest 2026-08-06). This well [2171,126,4200]..[2177,126,4206]
# is EMPTY at map rest (confirmed) — it only gets water once the Oasis survey is spiked
# (sidequest/kalahar/shut_pump latches oasis_pump_off). Forceload-then-schedule because the
# well is ~350 blocks from the Oasis where the player stands when the pump dies (unloaded chunks).
# Idempotent — re-setting water over water is harmless.
forceload add 2171 4200 2177 4206
schedule function cobblemon_initiative:oasis/second_well_fill_apply 2s

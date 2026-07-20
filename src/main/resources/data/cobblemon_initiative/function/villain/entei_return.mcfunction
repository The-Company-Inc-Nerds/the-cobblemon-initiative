# The Founder's shiny Entei comes home — fired from villain_boss's trainer-config
# command reward: `execute as {player} run function ...` at perm 4, so @s = the winner
# (PlayerProgressManager reward semantics; fires exactly once — the defeated set
# short-circuits, so no double-gift).
# It was the Founder's before the usurping; DJ only kept the kennel warm.
#
# The givemon itself moved into entei_show (0.6.0-alpha.8): givemon now opens the
# nickname prompt, and firing it here put the modal on top of the CURRENCY
# STABILIZED → ASSET REASSIGNED title choreography. The gift lands at the END of
# the reunion beat instead, so the naming ritual is its own moment.
tag @s add ci_entei_returned
# Defer the reunion title 5s so CURRENCY STABILIZED (economy/stabilized) owns the
# screen first and this beat lands alone after — the memory/frag_title precedent.
schedule function cobblemon_initiative:villain/entei_title 100t

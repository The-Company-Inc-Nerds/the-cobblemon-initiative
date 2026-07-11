# The Founder's shiny Entei comes home — fired from villain_boss's trainer-config
# command reward: `execute as {player} run function ...` at perm 4, so @s = the winner
# (PlayerProgressManager reward semantics; fires exactly once — the defeated set
# short-circuits, so no double-gift). NOT on the TBCS on_win: bare function calls
# there run with no player executor, and givemon needs a player source.
# It was the Founder's before the usurping; DJ only kept the kennel warm.
#
# IVs roll naturally; the fight copy's perfect 31s are DJ's grooming, not a
# contract on the gift.
cobblemon-initiative givemon entei level=64 shiny=true nature=adamant
tag @s add ci_entei_returned
# Defer the reunion title 5s so CURRENCY STABILIZED (economy/stabilized) owns the
# screen first and this beat lands alone after — the memory/frag_title precedent.
schedule function cobblemon_initiative:villain/entei_title 100t

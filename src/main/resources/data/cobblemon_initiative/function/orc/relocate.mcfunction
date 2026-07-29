# Roll a NEW random camp pin (a22). Called from orc/tick on a day change when the previous camp is
# cleared (#orc_active 0). Pins are the eight retired fixed sites, now a random pool. Reroll ONCE
# off the pin we just cleared so the camp visibly moves. Randomness on a spawn LOCATION is sanctioned
# (ENGINE_FINDINGS §3 — random is fine on spawn/flavor, banned on prices). Sets the active pin +
# clears the raised flag; orc/tick raises the warband when a player next comes within 48 of that pin.
execute store result score #orc_pick ci_ambient run random value 2..9
execute if score #orc_pick ci_ambient = #orc_last_pin ci_ambient store result score #orc_pick ci_ambient run random value 2..9
scoreboard players operation #orc_active ci_ambient = #orc_pick ci_ambient
scoreboard players set #orc_raised ci_ambient 0

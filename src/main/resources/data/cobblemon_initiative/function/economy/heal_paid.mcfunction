# Paid nurse healing. Run as the player (a healer NPC's heal button). Charges a flat fee,
# then fully heals the party. There is no hard balance gate (a dialog button cannot read a
# CobbleDollars balance), so the heal ALWAYS happens and the fee is best-effort - a broke
# player still gets patched up. Tune the one fee here and every nurse follows.
cobbledollars remove @s 100
healpokemon @s
title @s actionbar [{"text":"Your team is fully healed. ","color":"green"},{"text":"Service fee: 100 CobbleDollars.","color":"gray"}]

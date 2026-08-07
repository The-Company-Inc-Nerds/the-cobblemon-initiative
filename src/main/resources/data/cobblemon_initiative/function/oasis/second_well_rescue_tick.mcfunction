# Second well (M35) rescue tick — registered in #minecraft:tick. While the well stands EMPTY
# (before the Oasis is beaten = oasis_pump_off), any player who drops into the dry shaft is
# hauled back to the rim so they cannot get stuck at the bottom. Gated on !oasis_pump_off:
# once the water is back (pump beaten), jumping in is fine (it is a well full of water), so the
# tp self-disables. Single-flag guard matches the town-well pattern. Cheap: one box selector
# per tick, and the box only ever has an occupant if someone fell in.
# Shaft interior box = footprint 2172..2176 / y120..126 / 4201..4205 (the M35 marker box minus
# the liner). Rim tp target = 2174.5/127/4199.5, a safe standing spot just outside the mouth.
# NEEDS in-world verification of the shaft floor-y + a valid rim standing block (see followups).
execute as @a[x=2172,y=120,z=4201,dx=4,dy=6,dz=4,tag=!oasis_pump_off] at @s run tp @s 2174.5 127.0 4199.5
execute as @a[x=2172,y=120,z=4201,dx=4,dy=6,dz=4,tag=!oasis_pump_off] run title @s actionbar [{"text":"Someone hauls you out of the dry well before you break something.","color":"aqua"}]

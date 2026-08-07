# Scheduled by second_well_fill_arm (+2s, chunks force-loaded). Fills the M35 shaft with water.
# The well is empty at rest, so this is a pure ADD: water into the shaft column, mouth at y126
# down to the shaft floor. Box footprint = the marker box [2171,4200]..[2177,4206]; the shaft
# runs down from y126. WITHOUT a region scan we fill a conservative column (y120..126) that
# covers the visible shaft; `fill ... keep` never replaces solid liner blocks — it only water-
# fills the air cells, so a deeper/shallower true floor is safe (any liner stone is left alone).
# NEEDS in-world verification of the true shaft floor-y (see followups) — extend the low bound
# if the shaft is deeper than y120.
fill 2172 120 4201 2176 126 4205 minecraft:water keep
execute positioned 2174.0 127.0 4203.0 run particle minecraft:splash ~ ~ ~ 2.0 0.4 2.0 0.0 100
execute positioned 2174.0 127.0 4203.0 run particle minecraft:falling_water ~ ~1 ~ 2.0 0.6 2.0 0.0 50
execute positioned 2174.0 127.0 4203.0 run playsound minecraft:ambient.underwater.enter ambient @a[distance=..48] 2174.0 127.0 4203.0 0.8 1.1
execute positioned 2174.0 127.0 4203.0 run title @a[distance=..64] actionbar [{"text":"Far off, a second dry well remembers how to fill.","color":"aqua"}]
forceload remove 2171 4200 2177 4206

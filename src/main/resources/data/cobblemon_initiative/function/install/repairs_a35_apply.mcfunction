# repairs wave a35 — apply: place the Cyber City power-plant copper bulbs + levers (chunks
# forceloaded by the arm). Coords MIRROR data/cobblemon_initiative/powerplant/powerplant.json
# (bulbs idx 0-8, then the 8 switch levers) — the engine tracks those exact positions, so keep
# the two in lockstep if either ever moves. Bulbs = plain copper_bulb; the engine's 40t sync
# flips LIT to match its bool[9] once a player is near, so the placed state doesn't matter.
# Lever facing is COSMETIC (the engine's bool[9] is truth; the click is intercepted by
# position) — these wall facings are a best-guess from the room layout; eyeball in-world and
# flip any that pop or look wrong (adjust here + it re-applies on the next fresh install).

# ── 9 copper bulbs (idx 0-8) ──────────────────────────────────────────────────────
# Generator 1 trio (x=1414 column) — idx 0-2
setblock 1414 92 940 minecraft:copper_bulb
setblock 1414 92 941 minecraft:copper_bulb
setblock 1414 92 942 minecraft:copper_bulb
# Generator 2 trio (x=1434 column) — idx 3-5
setblock 1434 92 940 minecraft:copper_bulb
setblock 1434 92 941 minecraft:copper_bulb
setblock 1434 92 942 minecraft:copper_bulb
# Console (z=928 wall) — idx 6, 7, and 8 = the raised top bulb (y=93)
setblock 1423 92 928 minecraft:copper_bulb
setblock 1425 92 928 minecraft:copper_bulb
setblock 1424 93 928 minecraft:copper_bulb

# ── 8 levers ──────────────────────────────────────────────────────────────────────
# Generator 1 levers — mount on the gen-1 column, face out east
setblock 1416 91 940 minecraft:lever[face=wall,facing=east]
setblock 1416 90 941 minecraft:lever[face=wall,facing=east]
setblock 1416 91 942 minecraft:lever[face=wall,facing=east]
# Generator 2 levers — mount on the gen-2 column, face out west
setblock 1432 91 940 minecraft:lever[face=wall,facing=west]
setblock 1432 90 941 minecraft:lever[face=wall,facing=west]
setblock 1432 91 942 minecraft:lever[face=wall,facing=west]
# Console levers — mount on the z=928 console wall, face out south
setblock 1423 90 929 minecraft:lever[face=wall,facing=south]
setblock 1425 90 929 minecraft:lever[face=wall,facing=south]

# Engine takes over LIT via its own 40t sync. If a freshly-placed board ever needs the puzzle
# re-applied by hand: /cobblemon-initiative powerplant scramble (sets a solvable pattern).
forceload remove 1414 928 1434 942

# Zwiggo vanish (a22, playtest N4). Run AS the swampert body, AT its position, from
# ambient/tick every tick a zwiggo_joined player exists and a zwiggo_body still stands.
# Position-independent (no distance selector — the old @e[...,distance=..3] pop-tag missed
# when the body drifted, so the swampert never disappeared after recruit). FX first, then
# kill @s so it reads as a slip back into the harbour rather than a hard despawn.
particle minecraft:splash ~ ~0.8 ~ 0.6 0.6 0.6 0 60 force
particle minecraft:poof ~ ~0.8 ~ 0.4 0.6 0.4 0.03 30 force
playsound minecraft:entity.fishing_bobber.splash player @a[distance=..24] ~ ~ ~ 1 0.8
kill @s

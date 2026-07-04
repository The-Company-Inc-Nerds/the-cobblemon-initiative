# VERIFIED GROWTH (The Greenhouse Tour) — the catwalk reveal sensor. Register in #minecraft:tick.
# Any player standing in the catwalk volume who has not yet had the reveal gets it — the vista
# is the payload, not the docent (verifier fix: fire for anyone in the box; hz_tour_started only
# gates the completion stamp). Coords are the PRESUMED catwalk of the glass tower (Xiao Mei body
# stands at 1542,101,2108) — VERIFY the box against the in-world build before shipping. Absolute
# x/y/z with an explicit dy so the volume does not collapse to the function origin (night_watch fix).
execute as @a[tag=!wheat_named,x=1535,dx=15,y=98,dy=8,z=2101,dz=15] at @s run function cobblemon_initiative:sidequest/greenhouse/reveal

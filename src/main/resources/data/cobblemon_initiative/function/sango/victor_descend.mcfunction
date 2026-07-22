# Victor earns his transform → he leaves the grain tower. Runs AS the qualified player from
# ambient/tick (the five anti-Company gates). One-shot: this tags the player victor_descended,
# and ambient/tick's dispatch gates on tag=!victor_descended, so it can never re-enter.
# Consume the tower latch so he never re-spawns up top, arm the PATH latch (=0 pending) so his
# body reappears down at the reveal site the next time this player nears it, and best-effort
# remove the tower body (an unloaded-chunk miss is caught by the ambient/tick self-heal sweep).
scoreboard players set #amb_victor ci_ambient 1
scoreboard players set #amb_victor_path ci_ambient 0
tag @s add victor_descended
execute positioned 2522.5 131 2815.5 run kill @e[type=easy_npc:humanoid,tag=victor_apprentice,distance=..8]

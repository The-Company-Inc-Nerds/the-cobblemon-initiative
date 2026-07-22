# Garden warden wake — fires when a challenger readies a Pokéball at one of the four Hua Zhan
# living-statue wardens (called from each warden's battle button, BEFORE do:battle). Bursts the
# stone to life AT the warden entity (found within 3 blocks of its placement coord, so it lands on
# the body even if the placed pos drifts a hair) and leaves it glowing through the fight — the
# "the statue stirs" beat the wardens otherwise only got on win. Macro args {x,y,z} = the warden's
# placement coord. Called via the entity path (execute-as @initiator run function … {x,y,z}); the
# commands are self-contained (positioned + @e), so the executing source does not matter.
$execute positioned $(x) $(y) $(z) as @e[type=easy_npc:humanoid,distance=..3,limit=1,sort=nearest] at @s run particle minecraft:totem_of_undying ~ ~1.2 ~ 0.4 0.9 0.4 0.08 120 force
$execute positioned $(x) $(y) $(z) as @e[type=easy_npc:humanoid,distance=..3,limit=1,sort=nearest] at @s run particle minecraft:end_rod ~ ~1 ~ 0.3 0.8 0.3 0.02 60 force
$execute positioned $(x) $(y) $(z) as @e[type=easy_npc:humanoid,distance=..3,limit=1,sort=nearest] at @s run playsound minecraft:block.beacon.activate master @a[distance=..40] ~ ~ ~ 1 1.3
$execute positioned $(x) $(y) $(z) as @e[type=easy_npc:humanoid,distance=..3,limit=1,sort=nearest] at @s run playsound minecraft:block.grass.break master @a[distance=..40] ~ ~ ~ 1 0.8
$execute positioned $(x) $(y) $(z) as @e[type=easy_npc:humanoid,distance=..3,limit=1,sort=nearest] at @s run effect give @s minecraft:glowing 60 0 true

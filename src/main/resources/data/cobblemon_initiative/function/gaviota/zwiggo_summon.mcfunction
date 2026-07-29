# Zwiggo surfaces (a6, playtest N3). Run AS the player from Deckhand Rocco's "yes, I like mudkips"
# button. Spawns the swampert DEEP in the harbour at 400.5 49 3501.5; its CanFloat + FLOAT objective
# (ambient_float_rise) swims it UP to the surface on its own — "it just comes up out of the water,"
# no walk-up path (the pre-a22 'large jump' bug) and no quiet quay-latch (a22). Guarded import_new
# (one body). Bubble column + surfacing splash + a dolphin-jump cue sell the emerge. The player is at
# the quay so the spawn chunk is live. Recruit + vanish are unchanged (talk to it -> givemon ->
# ambient/tick zwiggo_pop kills it). VERIFY: FLOAT surfaces a cobblemon_npc from y49.
execute unless entity @e[type=easy_npc:cobblemon_npc,tag=zwiggo_body] run easy_npc preset import_new data easy_npc:preset/humanoid/zwiggo_swampert.npc.snbt 400.5 49 3501.5
particle minecraft:bubble_column_up 400.5 52 3501.5 0.6 3.0 0.6 0.1 140 force
particle minecraft:bubble 400.5 50 3501.5 0.7 1.5 0.7 0.1 90 force
particle minecraft:splash 400.5 62.5 3501.5 0.9 0.3 0.9 0 70 force
playsound minecraft:entity.dolphin.jump player @a[distance=..48] 400.5 62 3501.5 1 0.7
playsound minecraft:entity.player.splash player @a[distance=..48] 400.5 62 3501.5 1 0.8

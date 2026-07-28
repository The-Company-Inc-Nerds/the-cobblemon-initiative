# zwiggo_summon — the mudkip man whistles his swampert up out of the harbour (a21
# easter egg, P10-P12). Run AS the player from Zwiggo Man's yes button (bare function,
# as_player -> ExecAsUser perm 2); the player stands at the quay so the spawn chunk is
# live. Guarded import_new (aya_transform / starter stand-in pattern): exactly one body,
# tag zwiggo_body baked in the preset. He surfaces in the water at 400.8 62.1 3500.6
# (ground-probed: water at y62, air above) and his MOVE_BACK_TO_HOME objective walks him
# to the quayside home 406/64/3501 — ambient/tick carries the ~200t tp fallback plus the
# one-tick-later zwiggo_pop kill after the join.
execute unless entity @e[type=easy_npc:cobblemon_npc,tag=zwiggo_body] run easy_npc preset import_new data easy_npc:preset/humanoid/zwiggo_swampert.npc.snbt 400.8 62.1 3500.6
particle minecraft:splash 400.8 62.6 3500.6 0.7 0.4 0.7 0 90 force
particle minecraft:bubble 400.8 62.2 3500.6 0.5 0.3 0.5 0 40 force
particle minecraft:bubble_pop 400.8 62.8 3500.6 0.5 0.4 0.5 0.02 30 force
playsound minecraft:entity.player.splash player @a[distance=..40] 400.8 62.1 3500.6 1 0.8
playsound minecraft:entity.dolphin.jump player @a[distance=..40] 400.8 62.1 3500.6 1 0.7

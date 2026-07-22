# Mid-cutscene swap (victini_reveal cue @ tick 60): the silent apprentice flashes into
# Victini, ON CAMERA. Runs AS the player from the cutscene cue — the player/rig is elsewhere,
# so every coord here is ABSOLUTE at the reveal site (NEVER ~ ~ ~). Victini eye ~y107.8, so
# the FX are lifted ~+0.5..0.9 to land mid-body.
particle minecraft:end_rod 2536.5 106.9 2900.5 0.3 0.7 0.3 0.04 80 force
particle minecraft:flame 2536.5 106.5 2900.5 0.4 0.4 0.4 0.02 40 force
playsound minecraft:block.beacon.activate master @a[distance=..40] 2536.5 106.6 2900.5 1 1.4
playsound minecraft:entity.player.levelup master @a[distance=..40] 2536.5 106.6 2900.5 1 1.6
# Remove the humanoid apprentice (transported here by victor_transform) and reveal Victini.
kill @e[tag=victor_apprentice,type=!minecraft:player]
easy_npc preset import_new data easy_npc:preset/humanoid/victor_victini.npc.snbt 2536 106 2900

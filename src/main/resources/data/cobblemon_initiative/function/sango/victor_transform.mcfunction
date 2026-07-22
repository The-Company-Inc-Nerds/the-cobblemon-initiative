# Victor's reveal. Fires EXACTLY once — the FIRST line sets the one-shot player tag
# victor_transform_fired that ambient/tick gates on, so this can never re-enter and restart
# the scene (the old game-breaking infinite-restart loop).
#
# Victor now STANDS at his reveal coords (2536 106 2900 — his placement) instead of the
# grain tower, so the scene plays IN PLACE: no cross-map teleport, no reveal-site forceload
# (the player is right here talking to him, so the chunk is already live). The actual swap
# (FX + humanoid -> Victini) fires MID-CUTSCENE from a victini_reveal cue at tick 60
# (sango/victor_transform_swap), so the transformation is visible ON CAMERA. Runs AS the
# qualified player; the despawn-on-victini_joined path in ambient/tick still applies.
tag @s add victor_transform_fired
cutscene play victini_reveal

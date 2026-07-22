# Victor's reveal (note 4: relocated to 2536 106 2900). Called from ambient/tick AS the
# qualified player the tick after they tag themselves victor_transformed. TRANSPORT the
# humanoid to the reveal spot and force-load it, then play the cutscene. The actual swap
# (FX + humanoid -> Victini) fires MID-CUTSCENE from a victini_reveal cue at tick 60
# (sango/victor_transform_swap), so the transformation is visible ON CAMERA rather than
# already done before the scene. Guarded upstream (unless entity victor_victini) so this
# fires exactly once; the despawn-on-victini_joined path in ambient/tick still applies.
#
# Force-load first: the player is at the tower when they trigger this, so the reveal-site
# chunk may be unloaded and the tp/swap would no-op. Cleared 15s later by
# victor_forceload_clear (after the ~7.5s scene has settled the NPC in).
forceload add 2536 2900
# Transport the humanoid apprentice to the reveal site — he stands here on camera until the
# mid-scene swap. Selected by tag so it works regardless of the execution position.
tp @e[tag=victor_apprentice,type=!minecraft:player,limit=1] 2536 106 2900
cutscene play victini_reveal
schedule function cobblemon_initiative:sango/victor_forceload_clear 15s

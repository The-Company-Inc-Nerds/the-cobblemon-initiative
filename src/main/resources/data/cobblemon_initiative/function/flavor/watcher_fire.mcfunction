# One-shot: mark seen FIRST (so a mid-scene relog can never replay it), then roll the scene.
# Runs AS @s = the player (the watcher tick's `as @a`), which is the source cutscene play needs.
tag @s add watcher_seen_1
cutscene play shadow_watcher

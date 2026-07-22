# Bee-Swarm Wave Defense — per-tick driver. NEEDS tick registration (bee_watch/tick is added to
# data/minecraft/tags/function/tick.json). Tag-guarded, single-player: at most one active wave.
# run_tick executes AS AND AT the player so its selectors anchor on the defender, never on the
# function execution origin.
execute as @a[tag=bw_active] at @s run function cobblemon_initiative:sidequest/bee_watch/run_tick

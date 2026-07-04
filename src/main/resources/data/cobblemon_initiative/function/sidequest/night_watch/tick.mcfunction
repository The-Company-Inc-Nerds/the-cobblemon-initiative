# First Night Watch — per-tick driver. NEEDS tick registration (reported via
# tickFunctions; no tag JSON edited here). Two jobs:
#
# 1) GATE BRIDGE: strict farm_1 check -> farm_1_free player tag (dialog gates read tags,
#    not scoreboard holders). Tag add is idempotent and the selector no-ops once applied,
#    so this is duplicate-safe if Tenants of Record ships the same bridge on quest cadence.
execute if score farm_1 field_freed matches 1 as @a[tag=!farm_1_free] run tag @s add farm_1_free
#
# 2) WATCHER DRIVER: tag-guarded, single-player: at most one watcher. VERIFIER FIX (1):
#    run_tick executes as AND AT the player so its bbox selector anchors on the watcher,
#    never on the function execution origin.
execute as @a[tag=ci_watching] at @s run function cobblemon_initiative:sidequest/night_watch/run_tick

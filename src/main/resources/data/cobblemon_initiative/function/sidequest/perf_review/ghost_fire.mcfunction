# Ghost path, stage 2 — the scheduled callback (server context; the pending tag
# re-targets the player, so this survives whatever @s the scheduler lost).
execute as @a[tag=perf_review_ghost_pending] run function cobblemon_initiative:sidequest/perf_review/ghost_reward

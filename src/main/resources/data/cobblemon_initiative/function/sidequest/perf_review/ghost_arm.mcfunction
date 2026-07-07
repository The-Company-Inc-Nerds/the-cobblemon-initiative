# Ghost path, stage 1 — arm the deferred reveal. Run as the player the tick the badge
# lands with gym1_seen still 0. perf_review_resolved latches HERE (so the tick poller
# stops re-arming and the seen branch can never double-fire); the reward + title ride
# ghost_fire 5s later so the SILENT STAKEHOLDER card gets the screen to itself after
# the badge ceremony (A6 two-stage rule: triumph first, meta after).
tag @s add perf_review_resolved
tag @s add perf_review_ghost_pending
schedule function cobblemon_initiative:sidequest/perf_review/ghost_fire 5s

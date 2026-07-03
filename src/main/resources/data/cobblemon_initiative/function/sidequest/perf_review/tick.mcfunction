# Performance Review (Ghost or Sweep) — sentry watcher. Register in #minecraft:tick
# (orchestrator wires the tag). The four baked ladder trainers (takehara_trainer_1..4,
# Koji/Yuki/Shin/Taro) are npcsight-registered sight sentries tagged takehara_sentry at
# placement; NpcSightManager writes their can_see_player objective every tick and this
# poller reads it — the documented scoreboard-as-IPC pattern. Nothing here forces damage:
# a spotted player gets the sentry battle dialog (npcsight dialog mode), which is a
# player-opted engagement inside the Takehara safe zone.

# Newly seen: latch per sentry, mark the nearest player, warn on the actionbar.
execute as @e[tag=takehara_sentry,scores={can_see_player=1}] unless score @s gym1_latch matches 1 at @s run function cobblemon_initiative:sidequest/perf_review/spotted

# Sight lost: re-arm the sentry latch for the next sighting session.
execute as @e[tag=takehara_sentry,scores={can_see_player=0}] if score @s gym1_latch matches 1 run scoreboard players set @s gym1_latch 0

# Resolution at badge time (the leader chamber trigger — beating Cicada IS reaching the
# chamber, and the sentries cannot see into it, so the count is final when the badge lands).
# Ghost: never seen -> Silent Stakeholder cache. Sweep/seen: audit closes quietly; the
# Verification Bonus is claimed at the gym guide (dialog/sq_perf_review_guide.json).
execute as @a[tag=defeated_takehara_leader,tag=!perf_review_resolved] unless score @s gym1_seen matches 1.. run function cobblemon_initiative:sidequest/perf_review/ghost_reward
execute as @a[tag=defeated_takehara_leader,tag=!perf_review_resolved] if score @s gym1_seen matches 1.. run function cobblemon_initiative:sidequest/perf_review/audit_closed

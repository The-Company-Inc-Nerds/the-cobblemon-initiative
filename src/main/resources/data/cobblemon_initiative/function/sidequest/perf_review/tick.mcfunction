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
# Ghost: never seen -> Silent Stakeholder cache, DEFERRED 5s (ghost_arm schedules
# ghost_fire) so the SILENT STAKEHOLDER card lands after the badge ceremony instead of
# under it. Sweep/seen: audit closes quietly; the Verification Bonus is claimed at the
# Takehara gym guide (dialog/takehara_guide.json).
execute as @a[tag=defeated_takehara_leader,tag=!perf_review_resolved] unless score @s gym1_seen matches 1.. run function cobblemon_initiative:sidequest/perf_review/ghost_arm
execute as @a[tag=defeated_takehara_leader,tag=!perf_review_resolved] if score @s gym1_seen matches 1.. run function cobblemon_initiative:sidequest/perf_review/audit_closed

# SETUP CANARY (op-facing, once per world): the whole meta rides the four tower trainers
# being npcsight-registered AND entity-tagged takehara_sentry — a RELEASE-BLOCKING world
# setup step (VERIFICATION_RUNBOOK). If a player who can still resolve the review is
# inside the tower while ZERO tagged sentries exist, every run silently "ghosts" — warn
# loudly so a missed setup pass is caught on the first climb. Tower bounds bracket the
# baked ladder coords (x 2040..2070, y 130..170, z 2415..2515).
execute unless score #pr_sentry_warn quest_hud matches 1 unless entity @e[tag=takehara_sentry] if entity @a[tag=!perf_review_resolved,x=2040,y=130,dx=30,dy=40,z=2415,dz=100] run tellraw @a [{"text":"[setup] ","color":"red","bold":true},{"text":"Performance Review: no takehara_sentry bodies are tagged — the gym-1 sight meta cannot fire. Tag the four tower trainers and npcsight-register them (VERIFICATION_RUNBOOK world setup).","color":"gray"}]
execute unless score #pr_sentry_warn quest_hud matches 1 unless entity @e[tag=takehara_sentry] if entity @a[tag=!perf_review_resolved,x=2040,y=130,dx=30,dy=40,z=2415,dz=100] run scoreboard players set #pr_sentry_warn quest_hud 1

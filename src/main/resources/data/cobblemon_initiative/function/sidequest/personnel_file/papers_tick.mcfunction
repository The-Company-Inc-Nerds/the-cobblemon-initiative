# Lucian's ledger — the two archive scores. Register in #minecraft:tick (orchestrator
# wires the tag). Recomputed from scratch every tick from tags the quests already latch
# (band_tags precedent — cheap, tag reads only). No new state is ever written by hand:
# redo one by fixing the underlying tag, never these scores.
#
# alpha.26 audit rulings: the notices_filed / ci_notices lines (Incomplete File stage 2)
# and the memo_heard / memo_delivered lines (Memo 44-C, checkpoint eavesdrop) were cut
# with the never-placed ghost cast — those latches can no longer be granted anywhere.
#
# ci_papers_filed — lifetime filings at Lucian's desk (drives her two default-tier
#   "the ledger grows heavier" dialog entries via the compiler band tags
#   ci_papers_filed_gte_3 / ci_papers_filed_gte_6). Seven filing latches.
# ci_papers_held  — Company papers currently in hand and NOT yet filed (drives the
#   quest/render FILING DAY aggregate at >= 2 held; the individual deliver lines
#   suppress themselves against this score).

scoreboard players set @a ci_papers_filed 0
execute as @a[tag=docs_filed] run scoreboard players add @s ci_papers_filed 1
execute as @a[tag=letter_delivered] run scoreboard players add @s ci_papers_filed 1
execute as @a[tag=hz_minutes_filed] run scoreboard players add @s ci_papers_filed 1
execute as @a[tag=scrub_report_filed] run scoreboard players add @s ci_papers_filed 1
execute as @a[tag=manifest_paid] run scoreboard players add @s ci_papers_filed 1
execute as @a[tag=transition_paid] run scoreboard players add @s ci_papers_filed 1
execute as @a[tag=field_memo_filed] run scoreboard players add @s ci_papers_filed 1

scoreboard players set @a ci_papers_held 0
execute as @a[tag=carrying_dead_letter] run scoreboard players add @s ci_papers_held 1
execute as @a[tag=hz_minutes_heard,tag=!hz_minutes_filed] run scoreboard players add @s ci_papers_held 1
execute as @a[tag=yield_report_taken,tag=!scrub_report_filed] run scoreboard players add @s ci_papers_held 1
execute as @a[tag=took_route_manifest,tag=!manifest_paid] run scoreboard players add @s ci_papers_held 1
execute as @a[tag=field_1_transition_taken,tag=!transition_paid] run scoreboard players add @s ci_papers_held 1
execute as @a[tag=sting_memo_taken,tag=!field_memo_filed] run scoreboard players add @s ci_papers_held 1
# Stage-1 record set counts as one paper once all three originals are in hand.
execute as @a[tag=doc_portrait,tag=doc_ledger,tag=doc_charter,tag=!docs_filed] run scoreboard players add @s ci_papers_held 1

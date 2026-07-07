# The Incomplete File (stage 2) — Revision Notice counter. Register in #minecraft:load
# (orchestrator wires the tag; this file does NOT edit function tags itself).
# ci_notices = how many intact Revision Notices the player currently holds (0..3). A
# surveyor sighting while holding any (personnel_file/tick) zeroes it and strips the
# per-board notice tags, so Lucian stage 2 turn-in (gate ci_notices >= 3) only ever opens
# on a fully unseen pull. Idempotent: objectives add no-ops when already present.
scoreboard objectives add ci_notices dummy
# Lucian's ledger scores (recomputed from tags every tick by papers_tick):
# ci_papers_filed = lifetime filings (drives her band-tag dialog entries);
# ci_papers_held  = unfiled Company papers in hand (quest/render FILING DAY aggregate).
scoreboard objectives add ci_papers_filed dummy
scoreboard objectives add ci_papers_held dummy

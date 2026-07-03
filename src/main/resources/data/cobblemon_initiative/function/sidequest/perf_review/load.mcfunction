# Performance Review (Ghost or Sweep) — objectives. Register in #minecraft:load
# (orchestrator wires the tag; this file must NOT edit function tags itself).
# gym1_seen  (players): times a Takehara sentry marked the player. 0 at badge time = ghost.
# gym1_latch (sentries): per-NPC one-shot latch so one sighting session = one increment.
scoreboard objectives add gym1_seen dummy
scoreboard objectives add gym1_latch dummy

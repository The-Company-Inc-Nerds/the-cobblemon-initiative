# Field-liberation latches. Run once on datapack load (wired via minecraft:load tag).
# field_freed: per-field one-way latch (holder = field id, e.g. "hua_zhan_1"). Set to 1 the first
#   time a field is liberated so free_field is idempotent. Add fails silently if it already exists.
scoreboard objectives add field_freed dummy

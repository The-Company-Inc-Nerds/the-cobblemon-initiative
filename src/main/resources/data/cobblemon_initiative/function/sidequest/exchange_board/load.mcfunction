# Verified Weather - declare the exchange-board read counter (registered in #minecraft:load).
# Idempotent: 'objectives add' no-ops if it already exists (world data, persists across relog).
scoreboard objectives add ci_mm_reads dummy

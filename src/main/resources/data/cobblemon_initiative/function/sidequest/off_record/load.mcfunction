# Off the Record - stealth-errand objectives. Register in #minecraft:load (orchestrator
# wires the tag; this file does NOT edit function tags itself).
# obs_count = auditor sightings during the CURRENT carry (reset at each errand start; >=3
# halves that errand's pay). obs_cd = a per-player re-log cooldown (ticks) so one sighting
# is not counted every tick. The persistent off_record_blown TAG remembers any sighting at
# all (denies the clean-sweep bonus). Idempotent: objectives add no-ops when present.
scoreboard objectives add obs_count dummy
scoreboard objectives add obs_cd dummy
